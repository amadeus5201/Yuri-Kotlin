package com.mikuac.yuri.plugins.passive

import com.mikuac.shiro.annotation.GroupMessageHandler
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.yuri.config.ReadConfig
import com.mikuac.yuri.entity.UserBlackListEntity
import com.mikuac.yuri.enums.RegexCMD
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.repository.UserBlackListRepository
import com.mikuac.yuri.utils.CheckUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class BlackListManager : BotPlugin() {

    @Autowired
    private lateinit var repository: UserBlackListRepository

    @Autowired
    private lateinit var checkUtils: CheckUtils

    @GroupMessageHandler(cmd = RegexCMD.BLOCK_USER)
    fun blockHandler(bot: Bot, event: GroupMessageEvent) {
        if (event.userId in ReadConfig.config.base.adminList) {
            try {
                val blockUserId = getBlockUser(event) ?: return
                if (checkUtils.checkUserInBlackList(blockUserId)) {
                    bot.sendGroupMsg(event.groupId, "用户 $blockUserId 已处于封禁列表", false)
                    return
                }
                repository.save(UserBlackListEntity(0, blockUserId))
                bot.sendGroupMsg(event.groupId, "用户 $blockUserId 已封禁", false)
            } catch (e: YuriException) {
                bot.sendGroupMsg(event.groupId, e.message, false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            bot.sendGroupMsg(event.groupId, "该操作仅机器人管理员", false)
        }
    }

    @GroupMessageHandler(cmd = RegexCMD.UNBLOCK_USER)
    fun unblockHandler(bot: Bot, event: GroupMessageEvent) {
        if (event.userId in ReadConfig.config.base.adminList) {
            try {
                val blockUserId = getBlockUser(event) ?: return
                if (!checkUtils.checkUserInBlackList(blockUserId)) {
                    bot.sendGroupMsg(event.groupId, "用户 $blockUserId 未处于封禁列表", false)
                    return
                }
                repository.deleteByUserId(blockUserId)
                bot.sendGroupMsg(event.groupId, "用户 $blockUserId 已解封", false)
            } catch (e: YuriException) {
                bot.sendGroupMsg(event.groupId, e.message, false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            bot.sendGroupMsg(event.groupId, "该操作仅机器人管理员", false)
        }
    }

    private fun getBlockUser(event: GroupMessageEvent): Long? {
        val atList = event.arrayMsg.filter { "at" == it.type }
        if (atList.isEmpty()) throw YuriException("请 @ 一名需要封禁或解封的群成员")
        val atUserId = atList[0].data["qq"]
        if ("all" == atUserId) throw YuriException("笨蛋！")
        if (atUserId != null) return atUserId.toLong()
        return null
    }

}