package megamek.common.util.generator;

import megamek.common.Crew;

public class CrewSkillSummaryUtil {
    public static String getPilotSkillSummary(Crew crew, boolean rpgGunnery) {
        if (rpgGunnery) {
            return crew.getGunneryL() + "/" + crew.getGunneryM() + "/" + crew.getGunneryB() + "/" + crew.getPiloting();
        } else {
            return crew.getGunnery() + "/" + crew.getPiloting();
        }

    }
}
