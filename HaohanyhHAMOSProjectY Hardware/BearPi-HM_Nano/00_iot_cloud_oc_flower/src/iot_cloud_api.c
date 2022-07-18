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

#include <stdio.h>
#include <string.h>
#include <ctype.h>
#include <stdlib.h>
#include <unistd.h>
#include "ohos_init.h"
#include "cmsis_os2.h"

#include <cJSON.h>
#include "flower.h"
#include "huawei_iot.h"

#define CONFIG_APP_LIFETIME       60     ///< seconds
#define CONFIG_QUEUE_TIMEOUT      (5*1000)
static app_cb_t  g_app_cb;
static CLOUD_CommandCallBack  g_RcvCmdCallBack = NULL;
static osThreadId_t g_appCloudMainTaskId;

/**
 * @brief This function used to report the data to the cloud
 * 
*/
static void deal_report_msg(report_t *report)
{
    oc_mqtt_profile_service_t    service;
    oc_mqtt_profile_kv_t         temperature;
    oc_mqtt_profile_kv_t         humidity;
    oc_mqtt_profile_kv_t         smo;
    oc_mqtt_profile_kv_t         motor;

    if (g_app_cb.connected != 1) {
        return;
    }
    /* package the data */
    service.event_time = NULL;
    service.service_id = "AutoWater";
    service.service_property = &smo;
    service.nxt = NULL;

    smo.key = "Soil_Moisture";
    smo.value = &report->smo;
    smo.type = EN_OC_MQTT_PROFILE_VALUE_INT;
    smo.nxt = &temperature;

    temperature.key = "Temperature";
    temperature.value = &report->temp;
    temperature.type = EN_OC_MQTT_PROFILE_VALUE_INT;
    temperature.nxt = &humidity;

    humidity.key = "Humidity";
    humidity.value = &report->hum;
    humidity.type = EN_OC_MQTT_PROFILE_VALUE_INT;
    humidity.nxt = &motor;

    motor.key = "MotorStatus";
    motor.value = report->motor ? "ON" : "OFF";
    motor.type = EN_OC_MQTT_PROFILE_VALUE_STRING;
    motor.nxt = NULL;
    
    oc_mqtt_profile_propertyreport(NULL,&service);
    return;
}


/**
 * @brief This function used to deal with message received from the cloud
 *        we package the received data to the queue to do next step
*/
static int msg_rcv_callback(oc_mqtt_profile_msgrcv_t *msg)
{
    int    ret = 0;
    char  *buf;
    int    buf_len;
    app_msg_t *app_msg;

    if((NULL == msg)|| (msg->request_id == NULL) || (msg->type != EN_OC_MQTT_PROFILE_MSG_TYPE_DOWN_COMMANDS)){
        return ret;
    }
    printf("%s:msgType:%d msgLen:%d \r\n",__FUNCTION__, msg->type, msg->msg_len);

    buf_len = sizeof(app_msg_t) + strlen(msg->request_id) + 1 + msg->msg_len + 1;
    buf = malloc(buf_len);
    if(NULL == buf){
        return ret;
    }
    app_msg = (app_msg_t *)buf;
    buf += sizeof(app_msg_t);

    app_msg->msg_type = en_msg_cmd;
    app_msg->msg.cmd.request_id = buf;
    buf_len = strlen(msg->request_id);
    buf += buf_len + 1;
    memcpy(app_msg->msg.cmd.request_id, msg->request_id, buf_len);
    app_msg->msg.cmd.request_id[buf_len] = '\0';
    buf_len = msg->msg_len;
    app_msg->msg.cmd.payload = buf;
    memcpy(app_msg->msg.cmd.payload, msg->msg, buf_len);
    app_msg->msg.cmd.payload[buf_len] = '\0';

    ret = queue_push(g_app_cb.app_msg,app_msg,10);
    if(ret != 0){
        free(app_msg);
    }
    return ret;
}


/**
 * @brief deal the message received from the queue
 * 
*/
static void deal_cmd_msg(cmd_t *cmd)
{
    cJSON *obj_root;
    cJSON *obj_cmdname;
    cJSON *obj_paras;
    cJSON *obj_para;

    int cmdret = 1;
    int motorStatus = 0;
    oc_mqtt_profile_cmdresp_t cmdresp;
    obj_root = cJSON_Parse(cmd->payload);
    if (NULL == obj_root){
        goto EXIT_JSONPARSE;
    }

    obj_cmdname = cJSON_GetObjectItem(obj_root, "command_name");
    if (NULL == obj_cmdname){
        goto EXIT_CMDOBJ;
    }
    if (0 == strcmp(cJSON_GetStringValue(obj_cmdname), "AutoWater_Control_Pump")) {
        obj_paras = cJSON_GetObjectItem(obj_root, "Paras");
        if (NULL == obj_paras){
            goto EXIT_OBJPARAS;
        }
        obj_para = cJSON_GetObjectItem(obj_paras, "Motor");
        if (NULL == obj_para){
            goto EXIT_OBJPARA;
        }
        if (0 == strcmp(cJSON_GetStringValue(obj_para), "ON")) {
            motorStatus = CN_BOARD_SWITCH_ON;
        }
        else {
            motorStatus = CN_BOARD_SWITCH_OFF;           
        }
        if(g_RcvCmdCallBack != NULL){
            cmdret = g_RcvCmdCallBack(CLOUD_COMMAND_CONTROLMOTOR, motorStatus);
        }
    }

EXIT_OBJPARA:
EXIT_OBJPARAS:
EXIT_CMDOBJ:
    cJSON_Delete(obj_root);
EXIT_JSONPARSE:
    ///< do the response
    cmdresp.paras = NULL;
    cmdresp.request_id = cmd->request_id;
    cmdresp.ret_code = cmdret;
    cmdresp.ret_name = NULL;
    (void)oc_mqtt_profile_cmdresp(NULL, &cmdresp);
    return;
}

/**
 * @brief this is the cloud main task entry
 *        we deal all the message in the queue
*/
static int CloudMainTaskEntry(void *arg)
{
    app_msg_t *app_msg;
    uint32_t ret ;
    // receive the message from the queue ,maybe receive from the clould, or maybe from the local
    while (1) {
        app_msg = NULL;
        (void)queue_pop(g_app_cb.app_msg,(void **)&app_msg,0xFFFFFFFF);
        if (app_msg != NULL) {
            switch (app_msg->msg_type){
                case en_msg_cmd:
                    /* 调用命令处理函数，处理平台下发的命令 */
                    deal_cmd_msg(&app_msg->msg.cmd);
                    break;
                case en_msg_report:
                    /* 调用消息上报处理函数，向平台发送数据 */
                    deal_report_msg(&app_msg->msg.report);
                    break;
                default:
                    break;
            }
            free(app_msg);
        }
    }
    return 0;
}


int CLOUD_Init(void)
{
    int ret = -1;
    /* create a queue to buffer the data */
    g_app_cb.app_msg = queue_create("queue_rcvmsg",10,1);
    if(NULL ==  g_app_cb.app_msg){
        printf("Create receive msg queue failed");
        return ret;        
    }
    /* initialize the iot sdk */
    huawei_iot_init();
    /* create a task to deal the send message or received message */
    osThreadAttr_t attr;
    attr.attr_bits = 0U;
    attr.cb_mem = NULL;
    attr.cb_size = 0U;
    attr.stack_mem = NULL;
    attr.name = "IoTCloudMain";
    attr.stack_size = 1024*8;
    attr.priority = 24;
    if ((g_appCloudMainTaskId = osThreadNew(CloudMainTaskEntry, NULL, &attr)) == NULL) {
        printf("Falied to create report_task_entry!\n");
        return ret;
    }
    ret = 0;
    return ret;
}

int CLOUD_Deinit(void)
{
    int ret = -1;
    osThreadTerminate(g_appCloudMainTaskId);
    g_appCloudMainTaskId = NULL;
    queue_delete( g_app_cb.app_msg);
     g_app_cb.app_msg = NULL;
    return ret;
}

int CLOUD_Connect(const char *deviceID, const char *devicePwd, \
				  const char *serverIP, const char *serverPort, \
				  CLOUD_CommandCallBack cmdCallBack)
{
    int ret;
    oc_mqtt_profile_connect_t  connect_para;
    (void) memset( &connect_para, 0, sizeof(connect_para));

    connect_para.boostrap = 0;
    connect_para.device_id = deviceID;              
    connect_para.device_passwd = devicePwd;
    connect_para.server_addr = serverIP; 
    connect_para.server_port = serverPort;
    connect_para.life_time =  CONFIG_APP_LIFETIME;
    connect_para.rcvfunc = msg_rcv_callback;
    connect_para.security.type = EN_DTLS_AL_SECURITY_TYPE_NONE;
    ret = oc_mqtt_profile_connect(&connect_para);
    if ((ret == (int)en_oc_mqtt_err_ok)) {
        g_app_cb.connected = 1;  
        g_RcvCmdCallBack = cmdCallBack;
        ret = 0;    
    }
    else {
        printf("Huawei iot cloud connect faild!\r\n");
        ret = -1;
    }
    return ret;      
}

int CLOUD_Disconnect(void)
{
    int ret;
    ret = oc_mqtt_profile_disconnect();
    if (ret == (int)en_oc_mqtt_err_ok) {
        return 0;
    } else {
        return -1;
    }
}

int CLOUD_ReportMsg(int *airTemperature, int *airHumidity, int *soilMoisture, int *motorStatus)
{
    int ret = -1;
    static int curMotorStatus = 0;
    static int curAirTemperature = 0;
    static int curAireHumidity = 0;
    static int curSoilMoisture = 0;
    app_msg_t *app_msg;

    //update the data;
    if (airTemperature != NULL) {
        curAirTemperature = *airTemperature;
    }
    if (airHumidity != NULL) {
        curAireHumidity = *airHumidity;
    }
    if (soilMoisture != NULL) {
        curSoilMoisture = *soilMoisture;
    }
    if (motorStatus != NULL) {
        curMotorStatus = *motorStatus;
    }

    app_msg = malloc(sizeof(app_msg_t));
    if (NULL != app_msg) {
        app_msg->msg_type = en_msg_report;
        app_msg->msg.report.hum = curAireHumidity;
        app_msg->msg.report.temp = curAirTemperature;
        app_msg->msg.report.smo = curSoilMoisture;
        app_msg->msg.report.motor = curMotorStatus;
        if (0 != queue_push(g_app_cb.app_msg,app_msg,CONFIG_QUEUE_TIMEOUT)) {
            free(app_msg);
        } else {
            ret = 0;
        }

    }
    return ret;
}
