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
#ifndef __Flower_H__
#define __Flower_H__

/***************************************************************
* 名		称: GasStatus_ENUM
* 说    明：枚举状态结构体
***************************************************************/
typedef enum
{
	OFF = 0,
	ON
} Flower_Status_ENUM;

/* 传感器数据类型定义 ------------------------------------------------------------*/
typedef struct
{
	float    Smo;				//土壤湿度
	float    Humidity;        	//湿度
	float    Temperature;     	//温度
} Flower_Data_TypeDef;



/* 寄存器宏定义 --------------------------------------------------------------------*/
#define SHT30_Addr 0x44


void Flower_Init(void);
void Flower_Read_Data(Flower_Data_TypeDef *ReadData);
void Motor_StatusSet(Flower_Status_ENUM status);


#endif /* __Flower_H__ */

