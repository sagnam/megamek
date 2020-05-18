/*
 * MegaMek -
 * Copyright (C) 2005 Ben Mazur (bmazur@sev.org)
 * Copyright © 2014 Nicholas Walczak (walczak@cs.umn.edu)
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 2 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 */
package megamek.server.commands;

import megamek.common.IPlayer;
import megamek.server.Server;

/**
 * This command allows a player to allow another player to change the facing of one of their units.
 * 
 * @author arlith
 */
public class AllowFacingChangeCommand extends ServerCommand {

    public AllowFacingChangeCommand(Server server) {
        super(server, "allowFacingChange", "Allows a player to change the facing of a unit. "
                + "Usage: /allowFacingChange used in response to another " +
                "Player's request to change facing.  All players must allow the change.");
    }

    /**
     * Run this command with the arguments supplied
     * 
     * @see megamek.server.commands.ServerCommand#run(int, java.lang.String[])
     */
    @Override
    public void run(int connId, String[] args) {
        IPlayer player = server.getPlayer(connId);
        player.setAllowFacingChange(true);

        if (!server.isFacingChangeRequestInProgress()){
            server.sendServerChat(connId, "No vote to change " +
                    "facing in progress!");
            return;
        }

        // Tally votes
        boolean changeFacing = true;
        int voteCount = 0;
        int eligiblePlayerCount = 0;
        for (IPlayer p : server.getGame().getPlayersVector()){
            if (p.getTeam() != IPlayer.TEAM_UNASSIGNED) {
                changeFacing &= p.isAllowingFacingChange();
                if (p.isAllowingFacingChange()) {
                    voteCount++;
                }
                eligiblePlayerCount++;
            }
        }

        // Inform all players about the vote
        server.sendServerChat(player.getName() + " has voted to allow "
                + server.getUnitRequestingFacingChange().getDisplayName()
                + " to change facing to " + getDirection(server.getRequestedFacing())
                + ", " + voteCount
                + " vote(s) received out of " + eligiblePlayerCount
                + " vote(s) needed");

        // If all votes are received, perform facing change
        if (changeFacing){
            server.sendServerChat("All votes received, "
                    + server.getUnitRequestingFacingChange().getDisplayName()
                    + " turned to face " + server.getRequestedFacing() + ".");
            server.performFacingChange();
        }
    }


    public static String getDirection(int arg) {
        switch (arg) {
            case 0:
                return "N";
            case 1:
                return "NE";
            case 2:
                return "SE";
            case 3:
                return "S";
            case 4:
                return "SW";
            case 5:
                return "NW";
            default:
                return "Unk";
        }
    }
}
