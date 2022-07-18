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
#ifndef __IOT_HARDWARE_API_H__
#define __IOT_HARDWARE_API_H__

#define CN_BOARD_SWITCH_ON    1
#define CN_BOARD_SWITCH_OFF   0

/**
 * @brief Initialize the board wifi
 * @return Returns 0 success while others failed
 */
int BOARD_InitWifi(void);

/**
 * @brief Connect to the wifi
 * @param wifiSSID Indicates the ssid of the ap
 * @param wifiPWD Indicates the pwd of the ap
 * @return Returns 0 success while others failed
 */
int BOARD_ConnectWifi(const char *wifiSSID, const char *wifiPWD);

/**
 * @brief Disconnect from the wifi AP
 * @return Returns 0 success while others failed
 */
int BOARD_DisconnectWifi(void);

/**
 * @brief Initialize the board motor
 * @return Returns 0 success while others failed
 */
int BOARD_InitMotor(void);

/**
 * @brief Control the motor
 * @param status Indicates the status to set and  the value should be CN_BOARD_SWICT_ON/OFF
 * @return Returns 0 success while others failed
 */
int BOARD_SetMotorStatus(int status);

/**
 * @brief Get the motor status
 * @return Returns the status of the motor,the value should be CN_BOARD_SWICT_ON/OFF
 */
int BOARD_GetMotorStatus(void);

/**
 * @brief Initialize the board air sensor
 * @return Returns 0 success while others failed
 */
int BOARD_InitAirSensor(void);

/**
 * @brief Get the air status
 * @return Returns 0 success while others failed
 */
int BOARD_GetAirStatus(int *temperatur, int *humidity);

/**
 * @brief Initialize the board soil sensor
 * @return Returns 0 success while others failed
 */
int BOARD_InitSoilSensor(void);

/**
 * @brief Get the air status
 * @param moisture Indicates store the data
 * @return Returns 0 success while others failed
 */
int BOARD_GetSoilStatus(int *moisture);

/**
 * @brief Initialize the board NFC
 * @return Returns 0 success while others failed
 */
int BOARD_InitNfc(void);

/**
 * @brief Defines the nfc information
 */
typedef struct {
    const char *deviceID;
    const char *devicePWD;
    const char *wifiSSID;
    const char *wifiPWD;
}NfcInfo;

/**
 * @brief Get the nfc info
 * @param info Indicates the buffer to storage the data get from NFC
 * @return Returns 0 success while others failed
 */
int BOARD_GetNfcInfo(NfcInfo *info);

/**
 * @brief Initialize the LED
 * @return Returns 0 success while others failed
 */
int BOARD_InitLed(void);

/**
 * @brief Control the led status
 * @param status Indicates the status to set and value should be CN_BOARD_SWICT_ON/OFF
 * @return Returns 0 success while others failed
 */
int BOARD_SetLedStatus(int status);

#endif /* __IOT_HARDWARE_API_H__ */

