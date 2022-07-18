/*
 * Copyright (c) 2020 Huawei Device Co., Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 /* 受Haohanyh Computer Software Products Open Source LICENSE保护 https://git.haohanyh.top:3001/Haohanyh/LICENSE */

#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <math.h>
#include "cmsis_os2.h"
#include "flower.h"
#include "iot_errno.h"
#include "iot_gpio.h"
#include "iot_gpio_ex.h"
#include "iot_i2c.h"
#include "iot_i2c_ex.h"
#include "iot_adc.h"

#define WIFI_IOT_I2C_IDX_1 1
#define IOT_ADC_CHANNEL_6  6

/***************************************************************
* 函数名称: Flower_IO_Init
* 说    明: Flower_GPIO初始化
* 参    数: 无
* 返 回 值: 无
***************************************************************/
static void Flower_IO_Init(void)
{

    //设置GPIO_14的复用功能为普通GPIO
    IoTGpioSetFunc(14, IOT_GPIO_FUNC_GPIO_14_GPIO);

    //设置GPIO_14为输出模式
    IoTGpioSetDir(14, IOT_GPIO_DIR_OUT);

    IoTGpioSetOutputVal(14, 1);

    //GPIO_0复用为I2C1_SDA
    IoTGpioSetFunc(0, IOT_GPIO_FUNC_GPIO_0_I2C1_SDA);

    //GPIO_1复用为I2C1_SCL
    IoTGpioSetFunc(1, IOT_GPIO_FUNC_GPIO_1_I2C1_SCL);

    //baudrate: 400kbps
    IoTI2cInit(WIFI_IOT_I2C_IDX_1, 400000);

}

/***************************************************************
* 函数名称: SHT30_reset
* 说    明: SHT30复位
* 参    数: 无
* 返 回 值: 无
***************************************************************/
void SHT30_reset(void)
{
    uint8_t send_data[2] = {0x30, 0xA2};
    IoTI2cWrite(WIFI_IOT_I2C_IDX_1, (SHT30_Addr << 1) | 0x00, send_data, 2);
}

/***************************************************************
* 函数名称: Init_SHT30
* 说    明: 初始化SHT30，设置测量周期
* 参    数: 无
* 返 回 值: 无
***************************************************************/
void Init_SHT30(void)
{
    uint8_t send_data[2] = {0x22, 0x36};
    IoTI2cWrite(WIFI_IOT_I2C_IDX_1, (SHT30_Addr << 1) | 0x00, send_data, 2);
}

/***************************************************************
* 函数名称: SHT3x_CheckCrc
* 说    明: 检查数据正确性
* 参    数: data：读取到的数据
						nbrOfBytes：需要校验的数量
						checksum：读取到的校对比验值
* 返 回 值: 校验结果，0-成功		1-失败
***************************************************************/
static uint8_t SHT3x_CheckCrc(uint8_t data[], uint8_t nbrOfBytes, uint8_t checksum)
{

    uint8_t crc = 0xFF;
    uint8_t bit = 0;
    uint8_t byteCtr;
    const int16_t POLYNOMIAL = 0x131;
    //calculates 8-Bit checksum with given polynomial
    for (byteCtr = 0; byteCtr < nbrOfBytes; ++byteCtr)
    {
        crc ^= (data[byteCtr]);
        for (bit = 8; bit > 0; --bit)
        {
            if (crc & 0x80)
                crc = (crc << 1) ^ POLYNOMIAL;
            else
                crc = (crc << 1);
        }
    }

    if (crc != checksum)
        return 1;
    else
        return 0;
}

/***************************************************************
* 函数名称: SHT3x_CalcTemperatureC
* 说    明: 温度计算
* 参    数: u16sT：读取到的温度原始数据
* 返 回 值: 计算后的温度数据
***************************************************************/
static float SHT3x_CalcTemperatureC(uint16_t u16sT)
{

    float temperatureC = 0;

    u16sT &= ~0x0003;

    temperatureC = (175 * (float)u16sT / 65535 - 45);

    return temperatureC;
}

/***************************************************************
* 函数名称: SHT3x_CalcRH
* 说    明: 湿度计算
* 参    数: u16sRH：读取到的湿度原始数据
* 返 回 值: 计算后的湿度数据
***************************************************************/
static float SHT3x_CalcRH(uint16_t u16sRH)
{

    float humidityRH = 0;

    u16sRH &= ~0x0003;

    humidityRH = (100 * (float)u16sRH / 65535);

    return humidityRH;
}

/***************************************************************
* 函数名称: Flower_Init
* 说    明: 初始化Flower_
* 参    数: 无
* 返 回 值: 无
***************************************************************/
void Flower_Init(void)
{
    Flower_IO_Init();
    Init_SHT30();
}

/***************************************************************
* 函数名称: Flower_Read_Data
* 说    明: 测量土壤湿度、空气温度、空气湿度
* 参    数: 无
* 返 回 值: 无
***************************************************************/
void Flower_Read_Data(Flower_Data_TypeDef *ReadData)
{
    unsigned int ret;
    unsigned short adc;

    ret = IoTAdcRead(IOT_ADC_CHANNEL_6, &adc, IOT_ADC_EQU_MODEL_8, IOT_ADC_CUR_BAIS_DEFAULT, 0xff);
    if (ret != IOT_SUCCESS)
    {
        printf("ADC Read Fail\n");
    }

    ReadData->Smo = (float)adc/1500*100;

    uint8_t data[3];
    uint16_t dat, tmp;
    uint8_t SHT3X_Data_Buffer[6];

    IotI2cData sht30_i2c_data = {0};
    uint8_t send_data[2] = {0xE0, 0x00};
    sht30_i2c_data.sendBuf = send_data;
    sht30_i2c_data.sendLen = 2;
    sht30_i2c_data.receiveBuf = SHT3X_Data_Buffer;
    sht30_i2c_data.receiveLen = 6;
    IoTI2cWriteread(WIFI_IOT_I2C_IDX_1, (SHT30_Addr << 1) | 0x00, &sht30_i2c_data);

    data[0] = SHT3X_Data_Buffer[0];
    data[1] = SHT3X_Data_Buffer[1];
    data[2] = SHT3X_Data_Buffer[2];

    tmp = SHT3x_CheckCrc(data, 2, data[2]);
    if (!tmp)
    {
        dat = ((uint16_t)data[0] << 8) | data[1];
        ReadData->Temperature = SHT3x_CalcTemperatureC(dat);
    }

    data[0] = SHT3X_Data_Buffer[3];
    data[1] = SHT3X_Data_Buffer[4];
    data[2] = SHT3X_Data_Buffer[5];

    tmp = SHT3x_CheckCrc(data, 2, data[2]);
    if (!tmp)
    {
        dat = ((uint16_t)data[0] << 8) | data[1];
        ReadData->Humidity = SHT3x_CalcRH(dat);
    }
}


/***************************************************************
* 函数名称: Motor_StatusSet
* 说    明: 电机状态设置
* 参    数: status,ENUM枚举的数据
*									OFF,关
*									ON,开
* 返 回 值: 无
***************************************************************/
void Motor_StatusSet(Flower_Status_ENUM status)
{
    if (status == ON)

        //设置GPIO_14输出高电平打开电机
        IoTGpioSetOutputVal(14, 0);

    if (status == OFF)

        //设置GPIO_14输出低电平关闭电机
        IoTGpioSetOutputVal(14, 1);
}
