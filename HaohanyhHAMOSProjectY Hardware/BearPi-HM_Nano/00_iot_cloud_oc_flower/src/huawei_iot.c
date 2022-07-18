/* 受Haohanyh Computer Software Products Open Source LICENSE保护 https://git.haohanyh.top:3001/Haohanyh/LICENSE */

#include "huawei_iot.h"

void huawei_iot_init(void)
{
    dtls_al_init();
    mqtt_al_init();
    oc_mqtt_init();
}
