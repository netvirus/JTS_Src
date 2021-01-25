package org.mmocore.gameserver.network.lineage.clientpackets;

import org.mmocore.gameserver.model.Request;
import org.mmocore.gameserver.model.Request.L2RequestType;
import org.mmocore.gameserver.network.lineage.components.SystemMsg;
import org.mmocore.gameserver.network.lineage.serverpackets.L2Friend;
import org.mmocore.gameserver.network.lineage.serverpackets.SystemMessage;
import org.mmocore.gameserver.object.Player;

public class RequestFriendAddReply extends L2GameClientPacket {
    private int _response;

    @Override
    protected void readImpl() {
        _response = _buf.hasRemaining() ? readD() : 0;
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }

        final Request request = activeChar.getRequest();
        if (request == null || !request.isTypeOf(L2RequestType.FRIEND)) {
            return;
        }

        if (activeChar.isOutOfControl()) {
            request.cancel();
            activeChar.sendActionFailed();
            return;
        }

        if (!request.isInProgress()) {
            request.cancel();
            activeChar.sendActionFailed();
            return;
        }

        if (activeChar.isOutOfControl()) {
            request.cancel();
            activeChar.sendActionFailed();
            return;
        }

        final Player requestor = request.getRequester();
        if (requestor == null) {
            request.cancel();
            activeChar.sendPacket(SystemMsg.THE_USER_WHO_REQUESTED_TO_BECOME_FRIENDS_IS_NOT_FOUND_IN_THE_GAME);
            activeChar.sendActionFailed();
            return;
        }

        if (requestor.getRequest() != request) {
            request.cancel();
            activeChar.sendActionFailed();
            return;
        }

        if (_response == 0) {
            request.cancel();
            requestor.sendPacket(SystemMsg.YOU_HAVE_FAILED_TO_ADD_A_FRIEND_TO_YOUR_FRIENDS_LIST);
            activeChar.sendActionFailed();
            return;
        }

        requestor.getFriendComponent().addFriend(activeChar);
        activeChar.getFriendComponent().addFriend(requestor);

        requestor.sendPacket(SystemMsg.THAT_PERSON_HAS_BEEN_SUCCESSFULLY_ADDED_TO_YOUR_FRIEND_LIST, new SystemMessage(SystemMsg.S1_HAS_BEEN_ADDED_TO_YOUR_FRIEND_LIST).addString(activeChar.getName()), new L2Friend(activeChar, true));
        activeChar.sendPacket(new SystemMessage(SystemMsg.S1_HAS_JOINED_AS_A_FRIEND).addString(requestor.getName()), new L2Friend(requestor, true));
    }
}