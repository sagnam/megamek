/*
 * MegaMek - Copyright (C) 2000-2002 Ben Mazur (bmazur@sev.org)
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

/*
 * SkipCommand.java
 *
 * Created on February 19, 2003, 12:16 PM
 */

package megamek.server.commands;

import megamek.client.commands.ClientCommand;
import megamek.common.Entity;
import megamek.common.IGame.Phase;
import megamek.common.IPlayer;
import megamek.server.Server;

/**
 * Fix facing of unit.
 * 
 * @author sagnam
 * @version
 */
public class FixFacingCommand extends ServerCommand {
    public static String SERVER_VOTE_PROMPT_MSG = "All players "
            + "must allow this change.  Use /allowFacingChange "
            + "to allow this change.";


    /**
     * Creates a new instance of FixFacingCommand
     */
    public FixFacingCommand(Server server) {
        super(server, "fixFacing",
                "Fix facing of unit.  Usage: /fixFacing # <direction> where the first " +
                        "value is the entity id and the second is the new facing direction, one of "
                        + "[N,NE,SE,S,SW,NW]");
    }

    /**
     * Run this command with the arguments supplied
     *
     * @see megamek.server.commands.ServerCommand#run(int, java.lang.String[])
     */
    @Override
    public void run(int connId, String[] args) {
        try {
            if (args.length != 3) {
                server.sendServerChat(connId, "Incorrect number of arguments "
                        + "for fixFacing command!  Expected 2, received, "
                        + (args.length - 1) + ".");
                server.sendServerChat(connId, getHelp());
                return;
            }

            int eid = Integer.parseInt(args[1]);
            Entity ent = server.getGame().getEntity(eid);

            if (null == ent) {
                server.sendServerChat(connId, "No such entity");
                return;
            }


            IPlayer player = server.getPlayer(connId);
            String direction = args[2];
            int dir = ClientCommand.getDirection(direction);

            boolean deployedThisRound = ent.getDeployRound() == server.getGame().getRoundCount() ||
                    (ent.getDeployRound() == 0 && server.getGame().getRoundCount() == 1);
            boolean hasMoved = !(server.getGame().getPhase().isBefore(Phase.PHASE_MOVEMENT) ||
                    (server.getGame().getPhase() == Phase.PHASE_MOVEMENT && !ent.isDone()));

            if (ent.getOwner().getId() != connId) {
                server.sendServerChat(connId, "You must own an entity to change its facing.");
            } else if (!ent.isDeployed()) {
                server.sendServerChat(connId, "Can only change facing of entity that has been deployed.");
            } else if (!hasMoved && !deployedThisRound) {
                server.sendServerChat(connId, "Entity must have just deployed to change facing before movement.");
            } else if (hasMoved && (!ent.getIsJumpingNow() || ent.weaponFired())) {
                server.sendServerChat(connId, "An entity that has already moved must have jumped this round and not yet fired to change facing.");
            } else if (dir < 0 || dir > 5) {
                server.sendServerChat(connId, "Facing must be one of [N,NE,SE,S,SW,NW]");
            } else {
                for (IPlayer p : server.getGame().getPlayersVector()) {
                    server.sendServerChat(p.getId(), player.getName()
                            + " wants to change the facing of " + ent.getDisplayName()
                            + " to " + direction + ".  "
                            + SERVER_VOTE_PROMPT_MSG);
                }

                server.requestFacingChange(ent, dir);

                for (IPlayer p : server.getGame().getPlayersVector()) {
                    p.setAllowFacingChange(false);
                }

                player.setAllowFacingChange(true);

                boolean changeFacing = true;
                for (IPlayer p : server.getGame().getPlayersVector()) {
                    if (p.getTeam() != IPlayer.TEAM_UNASSIGNED) {
                        changeFacing &= p.isAllowingFacingChange();
                    }
                }

                // If all votes are received, perform facing change
                if (changeFacing){
                    server.sendServerChat(server.getUnitRequestingFacingChange().getDisplayName()
                            + " turned to face " + server.getRequestedFacing() + ".");
                    server.performFacingChange();
                }
            }
        } catch (NumberFormatException nfe) {
            server.sendServerChat(connId, "Failed to parse facing number!");
        }
    }

    public static int getDirection(String arg) {
        int face = -1;

        if (arg.equalsIgnoreCase("N")) {
            face = 0;
        } else if (arg.equalsIgnoreCase("NE")) {
            face = 1;
        } else if (arg.equalsIgnoreCase("SE")) {
            face = 2;
        } else if (arg.equalsIgnoreCase("S")) {
            face = 3;
        } else if (arg.equalsIgnoreCase("SW")) {
            face = 4;
        } else if (arg.equalsIgnoreCase("NW")) {
            face = 5;
        }

        return face;
    }
}