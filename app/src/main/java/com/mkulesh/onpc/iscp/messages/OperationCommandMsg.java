/*
 * Enhanced Music Controller
 * Copyright (C) 2018-2023 by Mikhail Kulesh
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details. You should have received a copy of the GNU General
 * Public License along with this program.
 */

package com.mkulesh.onpc.iscp.messages;

import com.mkulesh.onpc.R;
import com.mkulesh.onpc.iscp.EISCPMessage;
import com.mkulesh.onpc.iscp.ZonedMessage;
import com.mkulesh.onpc.utils.Utils;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

/*
 * Network/USB Operation Command (Network Model Only after TX-NR905)
 */
public class OperationCommandMsg extends ZonedMessage
{
    public final static String CODE = "NTC";
    private final static String ZONE2_CODE = "NTZ";
    private final static String ZONE3_CODE = "NT3";
    private final static String ZONE4_CODE = "NT4";

    private final static String[] ZONE_COMMANDS = new String[]{ CODE, ZONE2_CODE, ZONE3_CODE, ZONE4_CODE };

    public enum Command implements StringParameterIf
    {
        PLAY("PLAY", R.string.cmd_description_play, R.drawable.cmd_play),
        STOP("STOP", R.string.cmd_description_stop, R.drawable.cmd_stop),
        PAUSE("PAUSE", R.string.cmd_description_pause, R.drawable.cmd_pause),
        P_P("P/P", R.string.cmd_description_p_p),
        TRUP("TRUP", R.string.cmd_description_trup, R.drawable.cmd_next),
        TRDN("TRDN", R.string.cmd_description_trdn, R.drawable.cmd_previous),
        FF("FF", R.string.cmd_description_ff, R.drawable.cmd_fast_forward),
        REW("REW", R.string.cmd_description_rew, R.drawable.cmd_fast_backward),
        REPEAT("REPEAT", R.string.cmd_description_repeat, R.drawable.repeat_all),
        RANDOM("RANDOM", R.string.cmd_description_random, R.drawable.cmd_random),
        REP_SHF("REP/SHF", R.string.cmd_description_rep_shf),
        DISPLAY("DISPLAY", R.string.cmd_description_display),
        ALBUM("ALBUM", R.string.cmd_description_album),
        ARTIST("ARTIST", R.string.cmd_description_artist),
        GENRE("GENRE", R.string.cmd_description_genre),
        PLAYLIST("PLAYLIST", R.string.cmd_description_playlist),
        RIGHT("RIGHT", R.string.cmd_description_right, R.drawable.cmd_right),
        LEFT("LEFT", R.string.cmd_description_left, R.drawable.cmd_left),
        UP("UP", R.string.cmd_description_up, R.drawable.cmd_up),
        DOWN("DOWN", R.string.cmd_description_down, R.drawable.cmd_down),
        SELECT("SELECT", R.string.cmd_description_select, R.drawable.cmd_select),
        KEY_0("0", R.string.cmd_description_key_0),
        KEY_1("1", R.string.cmd_description_key_1),
        KEY_2("2", R.string.cmd_description_key_2),
        KEY_3("3", R.string.cmd_description_key_3),
        KEY_4("4", R.string.cmd_description_key_4),
        KEY_5("5", R.string.cmd_description_key_5),
        KEY_6("6", R.string.cmd_description_key_6),
        KEY_7("7", R.string.cmd_description_key_7),
        KEY_8("8", R.string.cmd_description_key_8),
        KEY_9("9", R.string.cmd_description_key_9),
        DELETE("DELETE", R.string.cmd_description_delete, R.drawable.cmd_delete),
        CAPS("CAPS", R.string.cmd_description_caps),
        LOCATION("LOCATION", R.string.cmd_description_location),
        LANGUAGE("LANGUAGE", R.string.cmd_description_language),
        SETUP("SETUP", R.string.cmd_description_setup, R.drawable.cmd_setup),
        RETURN("RETURN", R.string.cmd_description_return, R.drawable.cmd_return),
        CHUP("CHUP", R.string.cmd_description_chup),
        CHDN("CHDN", R.string.cmd_description_chdn),
        MENU("MENU", R.string.cmd_description_menu, R.drawable.cmd_track_menu),
        TOP("TOP", R.string.cmd_description_top, R.drawable.cmd_top),
        MODE("MODE", R.string.cmd_description_mode),
        LIST("LIST", R.string.cmd_description_list),
        MEMORY("MEMORY", R.string.cmd_description_memory),
        F1("F1", R.string.cmd_description_f1, R.drawable.feed_like),
        F2("F2", R.string.cmd_description_f2, R.drawable.feed_dont_like),
        SORT("SORT", R.string.cmd_description_sort, R.drawable.cmd_sort),

        /*
         * Denon control protocol
         */
        DCP_REPEAT_ALL("DCP_REPEAT_ALL", R.string.cmd_description_repeat, R.drawable.repeat_all),
        DCP_REPEAT_ONE("DCP_REPEAT_ONE", R.string.cmd_description_repeat, R.drawable.repeat_all),
        DCP_REPEAT_OFF("DCP_REPEAT_OFF", R.string.cmd_description_repeat, R.drawable.repeat_all),
        DCP_SHUFFLE_ON("DCP_SHUFFLE_ON", R.string.cmd_description_random, R.drawable.cmd_random),
        DCP_SHUFFLE_OFF("DCP_SHUFFLE_OFF", R.string.cmd_description_random, R.drawable.cmd_random);

        final String code;

        @StringRes
        final int descriptionId;

        @DrawableRes
        final int imageId;

        Command(final String code, @StringRes final int descriptionId)
        {
            this.code = code;
            this.descriptionId = descriptionId;
            this.imageId = -1;
        }

        Command(final String code, @StringRes final int descriptionId, @DrawableRes final int imageId)
        {
            this.code = code;
            this.descriptionId = descriptionId;
            this.imageId = imageId;
        }

        public String getCode()
        {
            return code;
        }

        @StringRes
        public int getDescriptionId()
        {
            return descriptionId;
        }

        @DrawableRes
        public int getImageId()
        {
            return imageId;
        }

        public boolean isImageValid()
        {
            return imageId != -1;
        }
    }

    private final Command command;

    OperationCommandMsg(EISCPMessage raw) throws Exception
    {
        super(raw, ZONE_COMMANDS);
        this.command = (Command) searchParameter(data, Command.values(), null);
    }

    public OperationCommandMsg(int zoneIndex, final String command)
    {
        super(0, null, zoneIndex);
        this.command = (Command) searchParameter(command, Command.values(), null);
    }

    public OperationCommandMsg(final Command command)
    {
        super(0, null, ReceiverInformationMsg.DEFAULT_ACTIVE_ZONE);
        this.command = command;
    }

    @Override
    public String getZoneCommand()
    {
        return ZONE_COMMANDS[zoneIndex];
    }

    public Command getCommand()
    {
        return command;
    }

    @NonNull
    @Override
    public String toString()
    {
        return getZoneCommand() + "[" + data
                + "; ZONE_INDEX=" + zoneIndex
                + "; CMD=" + (command == null ? "null" : command.toString()) + "]";
    }

    @Override
    public EISCPMessage getCmdMsg()
    {
        return command == null ? null : new EISCPMessage(getZoneCommand(), command.getCode());
    }

    @Override
    public boolean hasImpactOnMediaList()
    {
        switch (command)
        {
        case REPEAT:
        case RANDOM:
        case F1:
        case F2:
            return false;
        default:
            return true;
        }
    }

    /*
     * Denon control protocol
     * - Set Play State Command: heos://player/set_play_state?pid=player_id&state=play_state
     * - Play Next Command: heos://player/play_next?pid=player_id
     * - Play Previous Command: heos://player/play_previous?pid=player_id
     * - Set Play Mode Command: heos://player/set_play_mode?pid='player_id'&repeat=on_all_or_on_one_or_off&shuffle=on_or_off
     * - Get Music Sources Command: heos://browse/get_music_sources
     */
    @Nullable
    @Override
    public String buildDcpMsg(boolean isQuery)
    {
        switch (command)
        {
        case PLAY:
        case PAUSE:
        case STOP:
            return "heos://player/set_play_state?pid=" + DCP_HEOS_PID +
                    "&state=" + command.name().toLowerCase();
        case TRDN:
            return "heos://player/play_previous?pid=" + DCP_HEOS_PID;
        case TRUP:
            return "heos://player/play_next?pid=" + DCP_HEOS_PID;
        case DCP_REPEAT_ALL:
            return "heos://player/set_play_mode?pid=" + DCP_HEOS_PID + "&repeat=on_all";
        case DCP_REPEAT_ONE:
            return "heos://player/set_play_mode?pid=" + DCP_HEOS_PID + "&repeat=on_one";
        case DCP_REPEAT_OFF:
            return "heos://player/set_play_mode?pid=" + DCP_HEOS_PID + "&repeat=off";
        case DCP_SHUFFLE_ON:
            return "heos://player/set_play_mode?pid=" + DCP_HEOS_PID + "&shuffle=on";
        case DCP_SHUFFLE_OFF:
            return "heos://player/set_play_mode?pid=" + DCP_HEOS_PID + "&shuffle=off";
        case TOP:
        case RETURN:
            return "heos://browse/get_music_sources";
        }
        return null;
    }

    public static Command toggleRepeat(Utils.ProtoType protoType, PlayStatusMsg.RepeatStatus repeatStatus)
    {
        if (protoType == Utils.ProtoType.ISCP)
        {
            return Command.REPEAT;
        }
        else
        {
            switch (repeatStatus)
            {
            case OFF:
                return Command.DCP_REPEAT_ALL;
            case ALL:
                return Command.DCP_REPEAT_ONE;
            default:
                return Command.DCP_REPEAT_OFF;
            }
        }
    }

    public static Command toggleShuffle(Utils.ProtoType protoType, PlayStatusMsg.ShuffleStatus shuffleStatus)
    {
        if (protoType == Utils.ProtoType.ISCP)
        {
            return Command.RANDOM;
        }
        else
        {
            return shuffleStatus == PlayStatusMsg.ShuffleStatus.OFF ?
                    Command.DCP_SHUFFLE_ON : Command.DCP_SHUFFLE_OFF;
        }
    }
}
