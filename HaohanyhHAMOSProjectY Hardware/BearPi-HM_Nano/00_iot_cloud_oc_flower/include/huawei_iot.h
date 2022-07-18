/* 受Haohanyh Computer Software Products Open Source LICENSE保护 https://git.haohanyh.top:3001/Haohanyh/LICENSE */
#ifndef __HUAWE_IOT_H__
#define __HUAWE_IOT_H__
#include <dtls_al.h>
#include <mqtt_al.h>
#include <queue.h>
#include <oc_mqtt_al.h>
#include <oc_mqtt_profile.h>

typedef enum
{
    en_msg_cmd = 0,
    en_msg_report,
    en_msg_conn,
    en_msg_disconn,
}en_msg_type_t;

typedef struct
{
    char *request_id;
    char *payload;
} cmd_t;

typedef struct
{
    int smo;
    int temp;
    int hum;
} report_t;

typedef struct
{
    en_msg_type_t msg_type;
    union
    {
        cmd_t cmd;
        report_t report;
    } msg;
} app_msg_t;

typedef struct
{
    queue_t                     *app_msg;
    int                          connected;
    int                          motor;
}app_cb_t;

void huawei_iot_init(void);

#endif