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
#ifndef __IOT_CLOUD_API_H__
#define __IOT_CLOUD_API_H__

/**
 * @brief Enumeration all the command issued by the Cloud Platform
 */
typedef enum {
    CLOUD_COMMAND_SETTHRETHOLD = 0,
    CLOUD_COMMAND_CONTROLMOTOR,
    CLOUD_COMMAND_LAST,
}CLOUD_CommandType;

/**
 * @brief Initialize the cloud sdk
 * @return 0 success while others failed
 */
int CLOUD_Init(void);
/**
 * @brief Do deinitialize the cloud sdk
 * @return 0 success while others failed
 */
int CLOUD_Deinit(void);

/**
 * @brief Callback function used to process commands delivered by the Cloud Platform
 * @param command Indicates the command delivered by the Cloud Platform
 * @param value Indicates the parameter corresponding to the command
 * @return Returns 0 success while -1 failed
 */
typedef int (*CLOUD_CommandCallBack)(int command, int value);

/**
 * @brief Send collected data to Cloud Platform
 * @param airTemperature Indicates the current local temperature, if NULL, will not update
 * @param airHumidity Indicates the current local humiditym, if NULL, will not update
 * @param soilMoisture Indicates the current soil moisture, if NULL, will not update
 * @param motorStatus Indicates the current motor status, if NULL, will not update
 * @return Returns 0 success while others failed
*/
int CLOUD_ReportMsg(int *airTemperature, int *airHumidity, int *soilMoisture, int *motorStatus);

/**
 * @brief Connect to the Cloud Platform
 * @param deviceID Indicats the deviceID create in the iot platform
 * @param devicePwd Indicates the corresponding to the deviceID
 * @param serverIP Indicates the ip of the iot platform
 * @param serverPort Indicates the port correspond to the ip
 * @param cmdCallBack Indicates command callback and will be called if any message comes
 * @return Returns 0 success while others failed
*/
int CLOUD_Connect(const char *deviceID, const char *devicePwd, \
    const char *serverIP, const char *serverPort, \
    CLOUD_CommandCallBack cmdCallBack);
/**
 * @brief Disconnect from the Cloud Platform
 * @return 0 success while others failed
*/
int CLOUD_Disconnect(void);
#endif /* __IOT_CLOUD_API_H__ */

