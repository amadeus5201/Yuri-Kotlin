package com.mikuac.yuri.common.config

data class Config(
    val base: Base,
    val command: Command,
    val plugin: Plugin
) {
    data class Base(
        val debug: Boolean,
        val adminList: List<Long>,
        val botName: String,
        val botSelfId: Long
    )

    data class Command(
        val prefix: String
    )

    data class Plugin(
        val eroticPic: EroticPic,
        val repeat: Repeat,
    ) {
        data class EroticPic(
            val r18: Boolean,
            val cdTime: Int,
            val recallMsgPicTime: Int
        )

        data class Repeat(
            val waitTime: Int,
            val thresholdValue: Int
        )
    }
}
