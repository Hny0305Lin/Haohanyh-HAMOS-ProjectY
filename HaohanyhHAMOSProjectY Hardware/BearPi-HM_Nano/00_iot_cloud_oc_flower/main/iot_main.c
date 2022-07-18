
/*
 * Copyright (c) 2020 Nanjing Xiaoxiongpai Intelligent Technology Co., Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/* 受Haohanyh Computer Software Products Open Source LICENSE保护 https://git.haohanyh.top:3001/Haohanyh/LICENSE */


#include "iot_cloud_api.h"
#include "iot_hardware_api.h"
#include "ohos_init.h"
#include <stdio.h>
#include <string.h>
#include <ctype.h>
#include <stdlib.h>
#include <unistd.h>
#include <cmsis_os2.h>

// DEFAULT PARAM, AND MUST BE MODIFIED TO THE REAL PARAMS
#define CONFIG_CLOUD_DEFAULT_SERVERIP "121.36.42.100"    // replace it with iotplatform server ip
#define CONFIG_CLOUD_DEFAULT_SERVERPORT "1883"           // replace it with iotplatform server  port
#define CONFIG_MOTOR_TIMELIMIT 4000                      // unit:minisecond
#define CONFIG_SENSOR_SAMPLE_CYCLE 3000                  // unit:minisecond
#define CONFIG_LED_FLASH_BASECYCLE 1000                  // unit:minisecond
#define CONFIG_TASK_DEFAULT_STACKSIZE 0x1000             // unit:bytes
#define CONFIG_TASK_DEFAULT_PRIOR 20                     // default task priority
#define CONFIG_TASK_MAIN_STACKSIZE 0x4000                // main task stacksize must be bigger
#define CONFIG_TASK_MAIN_PRIOR 25                        // default task priority
#define CN_MINISECONDS_IN_SECOND 1000                    // mini seconds in second
#define CN_LED_FLASH_STATUSNUM 2                         // LED flash:on/off
#define CN_SENSOREVENT_MOTORSTOP (1<<0)                  // report the motor status
#define CN_LED_STATUS_HARDINIT 2                         // unit: led base cycle
#define CN_LED_STATUS_CONNECTWIFI 4                      // unit: led base cycle
#define CN_LED_STATUS_CONNECTCLOUD 8                     // unit: led base cycle
#define CN_LED_STATUS_LOOPSAMPLE 16                      // unit: led base cycle
typedef enum {
    IOTMAIN_STATUS_HARDINIT = 1,
    IOTMAIN_STATUS_CONNECTWIFI,
    IOTMAIN_STATUS_CONNECTCLOUD,
    IOTMAIN_STATUS_LOOPSAMPLE,
}IoTMainStatus;
static IoTMainStatus g_iotMainStatus = IOTMAIN_STATUS_HARDINIT;  // Represent the main flow status
static osTimerId_t g_motorMonitorTimer;                          // Soft timer to monitor the motor status
static osEventFlagsId_t g_sensorTaskEvent;                       // The sensor collect data trigger event

/**
 * @brief Convert miniseconds to system ticks
 * @param ms Indicates the mimiseconds to convert
 * @return Returns the corresponding ticks of specified time
 */
static uint32_t Time2Tick(uint32_t ms)
{
    uint64_t ret;
    ret = ((uint64_t)ms * osKernelGetTickFreq()) / CN_MINISECONDS_IN_SECOND;
    return (uint32_t)ret;
}

/**
 @brief Set the main task flow status
 */
static void SetMainStatus(IoTMainStatus newStatus)
{
    g_iotMainStatus = newStatus;
}

/**
 * @brief LED flashing task entry
 */
static void LedTaskEntry(const void *arg)
{
    uint32_t baseTicks;
    uint64_t delayTicks;

    (void)arg;
    BOARD_InitLed();
    baseTicks = Time2Tick(CONFIG_LED_FLASH_BASECYCLE) / CN_LED_FLASH_STATUSNUM;
    while (1) {
        switch (g_iotMainStatus) {
            case IOTMAIN_STATUS_HARDINIT:
                delayTicks = CN_LED_STATUS_HARDINIT * (uint64_t)baseTicks;
                break;
            case IOTMAIN_STATUS_CONNECTWIFI:
                delayTicks = CN_LED_STATUS_CONNECTWIFI * (uint64_t)baseTicks;
                break;
            case IOTMAIN_STATUS_CONNECTCLOUD:
                delayTicks = CN_LED_STATUS_CONNECTCLOUD * (uint64_t)baseTicks;
                break;
            case IOTMAIN_STATUS_LOOPSAMPLE:
                delayTicks = CN_LED_STATUS_LOOPSAMPLE * (uint64_t)baseTicks;
                break;
            default:
                delayTicks = (uint64_t)baseTicks;
                break;
        }
        BOARD_SetLedStatus(CN_BOARD_SWITCH_ON);
        osDelay((uint32_t)delayTicks);
        BOARD_SetLedStatus(CN_BOARD_SWITCH_OFF);
        osDelay((uint32_t)delayTicks);
    }
    return;
}

/**
 * @brief Sensor data collection task entry and the main flow is as follows:
 *        1, Wait the for the event to trigger in specified time
 *        2, Collect Sensor Data (air humidity/temperature, soil moisture, etc.)
 *        3, Report the collected data to the Cloud Platform
 */
static void SensorTaskEntry(const void *arg)
{
    int temperature, humidity, moisture, motorStatus;
    int *temperatureBuf = NULL;
    int *humidityBuf = NULL;
    int *moistureBuf = NULL;
    int *motorStatusBuf = NULL;
    uint32_t sensorEvent;
    (void)arg;

    while (1) {
        temperatureBuf = NULL;
        humidityBuf = NULL;
        moistureBuf = NULL;
        motorStatusBuf = NULL;

        sensorEvent = osEventFlagsWait(g_sensorTaskEvent, CN_SENSOREVENT_MOTORSTOP, osFlagsWaitAny,\
                                       Time2Tick(CONFIG_SENSOR_SAMPLE_CYCLE));
        if (sensorEvent & CN_SENSOREVENT_MOTORSTOP) {
            motorStatus = CN_BOARD_SWITCH_OFF;
            motorStatusBuf = &motorStatus;
        }
        if (BOARD_GetAirStatus(&temperature, &humidity) == 0) {
            temperatureBuf = &temperature;
            humidityBuf = &humidity;
        }
        if (BOARD_GetSoilStatus(&moisture) == 0) {
            moistureBuf = &moisture;
        }

        (void) CLOUD_ReportMsg(temperatureBuf, humidityBuf, moistureBuf, motorStatusBuf);
    }
    return;
}

/**
 * @brief Callback function used to process commands delivered by the Cloud Platform
 * @param command Indicates the command delivered by the Cloud Platform
 * @param value Indicates the parameter corresponding to the command
 * @return Returns 0 success while -1 failed
 */
static int CommandCallBack(int command, int value)
{
    /**
     * Here, we only process the command for controlling the motor issued by the Cloud Platform, and the
     * value should be CN_BOARD_SWITCH_ON or CN_BOARD_SWITCH_OFF
     */
    if ((CLOUD_CommandType)command == CLOUD_COMMAND_CONTROLMOTOR) {
        if (value == CN_BOARD_SWITCH_ON) {
            /**
            * If the command is to start the motor, the soft timer needs to be started. Turn off the
            * motor when soft timer times out
            */
            osTimerStart(g_motorMonitorTimer, Time2Tick(CONFIG_MOTOR_TIMELIMIT));
        }
        return BOARD_SetMotorStatus(value);
    }
    return -1;
}

/**
 * @brief Callback function for motor monitor soft timer.
 *        When soft timer times out, we stop the motor and trigger a stop event for the sensor task
 */
static void MotorMonitorTimerCallBack(const void *arg)
{
    (void)arg;
    osTimerStop (g_motorMonitorTimer);
    BOARD_SetMotorStatus(CN_BOARD_SWITCH_OFF);
    osEventFlagsSet(g_sensorTaskEvent, CN_SENSOREVENT_MOTORSTOP);
}

/**
 * @brief IoTMain task entry and the main flow is as follows:
 *        1, Initialize the necessary hardware and obtain the corresponding information from NFC
 *        2, Connecting to wifi
 *        3, Connecting to the HUAWEI CLOUD Platform
 *        4, Creating a Sensor Collection Task
 */
static void IotMainTaskEntry(const void *arg)
{
    osThreadAttr_t attr;
    NfcInfo nfcInfo;
    (void)arg;

    SetMainStatus(IOTMAIN_STATUS_HARDINIT);
    BOARD_InitNfc();
    BOARD_InitMotor();
    BOARD_InitAirSensor();
    BOARD_InitSoilSensor();
    BOARD_InitWifi();
    if (BOARD_GetNfcInfo(&nfcInfo) != 0) {
        return;
    }

    SetMainStatus(IOTMAIN_STATUS_CONNECTWIFI);
    if (BOARD_ConnectWifi(nfcInfo.wifiSSID, nfcInfo.wifiPWD) != 0) {
        return;
    }

    SetMainStatus(IOTMAIN_STATUS_CONNECTCLOUD);
    if (CLOUD_Init() != 0) {
        return;
    }
    if (CLOUD_Connect(nfcInfo.deviceID, nfcInfo.devicePWD, \
        CONFIG_CLOUD_DEFAULT_SERVERIP, CONFIG_CLOUD_DEFAULT_SERVERPORT, \
        CommandCallBack) != 0) {
        return;
    }

    attr.attr_bits = 0U;
    attr.cb_mem = NULL;
    attr.cb_size = 0U;
    attr.stack_mem = NULL;
    attr.stack_size = CONFIG_TASK_DEFAULT_STACKSIZE;
    attr.priority = CONFIG_TASK_DEFAULT_PRIOR;
    attr.name = "SensorSample";
    if (osThreadNew((osThreadFunc_t)SensorTaskEntry, NULL, (const osThreadAttr_t *)&attr) == NULL) {
        return;
    }

    SetMainStatus(IOTMAIN_STATUS_LOOPSAMPLE);
    return;
}

/**
 * @brief IoT Main Entry of the IoT-Flower project
 */
static void IotMainEntry(void)
{
    osThreadAttr_t attr;

    // Create the motor monitor timer and sync event.
    g_motorMonitorTimer = osTimerNew((osTimerFunc_t)MotorMonitorTimerCallBack, osTimerPeriodic, NULL, NULL);
    if (g_motorMonitorTimer == NULL) {
        return;
    }
    g_sensorTaskEvent = osEventFlagsNew(NULL);
    if (g_sensorTaskEvent == NULL) {
        return;
    }

    // Create the Led flashing task and the iot main flow task
    attr.attr_bits = 0U;
    attr.cb_mem = NULL;
    attr.cb_size = 0U;
    attr.stack_mem = NULL;
    attr.stack_size = CONFIG_TASK_DEFAULT_STACKSIZE;
    attr.priority = CONFIG_TASK_DEFAULT_PRIOR;
    attr.name = "LedFlash";
    (void) osThreadNew((osThreadFunc_t)LedTaskEntry, NULL, (const osThreadAttr_t *)&attr);

    attr.stack_size = CONFIG_TASK_MAIN_STACKSIZE;
    attr.priority = CONFIG_TASK_MAIN_PRIOR;
    attr.name = "IoTMain";
    (void) osThreadNew((osThreadFunc_t)IotMainTaskEntry, NULL, (const osThreadAttr_t *)&attr);

    return;
}
// Add the IotMainEntry function to the System Startup Procedure
APP_FEATURE_INIT(IotMainEntry);