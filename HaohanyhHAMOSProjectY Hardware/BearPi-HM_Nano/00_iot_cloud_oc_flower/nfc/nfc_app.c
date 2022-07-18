/* 受Haohanyh Computer Software Products Open Source LICENSE保护 https://git.haohanyh.top:3001/Haohanyh/LICENSE */

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "nfc_app.h"
#include "NT3H.h"
#include <cJSON.h>


char product[50],wifissid[50], wifipwd[50],deviceid[50], devicepwd[50];
/**************************************************
 * get_devived_data
 * 获取WiFi的ssid和密钥及设备DevicesID和密钥
 * ***********************************************/
uint8_t nfc_get_devived_data(void)
{
    uint8_t memary_buf[16 * 15];
    cJSON *json, *jsonTemp; // *jsonArray,
    uint8_t jsonbuf[512] = {0};
    char jsonbuf_string[512] = {0};
    uint8_t payload_len = 0;
    uint8_t json_len = 0;

    NT3H1101_Read_Userpages(15, memary_buf);

    payload_len = memary_buf[4];
    json_len = payload_len - 3;

    //取出真实的json数据包，往后再偏移3位
    for (uint8_t i = 0; i < json_len; i++)
    {
        jsonbuf[i] = memary_buf[9 + i];

    }
    memset(jsonbuf_string, 0x00, sizeof(jsonbuf_string));
    sprintf(jsonbuf_string, "%s", jsonbuf);

    //解析json数据
    json = cJSON_Parse(jsonbuf_string);
    if (!json)
    {
        printf("Error before: [%s]\n", cJSON_GetErrorPtr());
        return -1;
    }
    else
    {
        jsonTemp = cJSON_GetObjectItem(json, "product");
        memset(product, 0, sizeof(product));
        snprintf(product, strlen(jsonTemp->valuestring) + 1, "%s", jsonTemp->valuestring);
        jsonTemp = cJSON_GetObjectItem(json, "device_id");
        memset(deviceid, 0, sizeof(deviceid));
        snprintf(deviceid, strlen(jsonTemp->valuestring) + 1, "%s", jsonTemp->valuestring);
        jsonTemp = cJSON_GetObjectItem(json, "secret");
        memset(devicepwd, 0, sizeof(devicepwd));
        snprintf(devicepwd, strlen(jsonTemp->valuestring) + 1, "%s", jsonTemp->valuestring);
        jsonTemp = cJSON_GetObjectItem(json, "ssid");
        memset(wifissid, 0, sizeof(wifissid));
        snprintf(wifissid, strlen(jsonTemp->valuestring) + 1, "%s", jsonTemp->valuestring);
        jsonTemp = cJSON_GetObjectItem(json, "pwd");
        memset(wifipwd, 0, sizeof(wifipwd));
        snprintf(wifipwd, strlen(jsonTemp->valuestring) + 1, "%s", jsonTemp->valuestring);


        cJSON_Delete(json);
        free(json); // isequal
    }
    return 0;
}
