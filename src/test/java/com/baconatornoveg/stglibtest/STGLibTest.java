package com.baconatornoveg.stglibtest;

import com.baconatornoveg.stglib.Item;
import com.baconatornoveg.stglib.Player;
import com.baconatornoveg.stglib.SmiteTeamGenerator;
import com.baconatornoveg.stglib.Team;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.fail;

public class STGLibTest {

    private SmiteTeamGenerator stg;
    private List<String> gods;
    private List<String> items;
    private List<String> boots;
    private int godsInitialSize;
    private int itemsInitialSize;
    private final int maxTests = 1000000;
    private Player player;

    public STGLibTest() {
        System.out.println("Running tests");
        gods = new ArrayList<>();
        items = new ArrayList<>();
        stg = new SmiteTeamGenerator();
        stg.getLists(true);
        gods = stg.getGodsAsStrings();
        items = stg.getItemsAsStrings();
        boots = stg.getBootsAsStrings();
        stg.buildType = 0;
        stg.isForcingBalanced = false;
        godsInitialSize = gods.size();
        itemsInitialSize = items.size();
    }

    @Test
    public void buildPrintOuts() {
        int total = 10;
        for (int i = 0; i < total; i++) {
            player = stg.makePlayer(stg.getGods(1, false).get(0));
            System.out.println(player);
        }
    }

    @Test
    public void buildList() {
        int total = 10000;
        String buildsCsvString = "";
        for (int i = 0; i < total; i++) {
            player = stg.makePlayer(stg.getGods(1, false).get(0));
            buildsCsvString += player.getGod().getName();
            for (String item : player.getBuild()) {
                buildsCsvString += "," + item;
            }
            buildsCsvString += "\n";
        }
        try {
            PrintWriter out = new PrintWriter("buildprintouts.csv");
            out.println(buildsCsvString);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testForAllGods() {

        int totalAttempts = 0;

        for (int i = 0; i < maxTests; i++) {
            totalAttempts++;
            player = stg.makePlayer(stg.getGods(1, false).get(0));
            String god = player.getGod().getName();
            //System.out.println(god);
            for (String g : gods) {
                if (god.equals(g)) {
                    gods.remove(g);
                    break;
                }
            }
            if (gods.size() <= 0) {
                break;
            }
        }
        if (gods.size() > 0) {
            System.err.println("Failed to confirm all " + godsInitialSize + " gods being generated in " + maxTests + " total generations.");
            System.err.println("Leftover gods: " + gods.toString());
            fail();
        } else {
            System.out.println("Successfully confirmed all " + godsInitialSize + " gods being generated in " + totalAttempts + " generations.");
            System.out.println("Leftover gods: " + gods.toString());
        }
    }

    @Test
    public void testForAllItems() {
        int totalAttempts = 0;
        List<String> testedItems = new ArrayList<>();
        List<String> missingItems = items;
        missingItems.addAll(boots);
        missingItems.add("Acorn of Yggdrasil");
        for (int i = 0; i < maxTests; i++) {
            totalAttempts++;
            player = stg.makePlayer(stg.getGods(1, false).get(0));
            List<String> build = player.getBuild();
            for (String item : items) {
                for (String itm : build) {
                    if (itm.equals(item) && !testedItems.contains(item)) {
                        testedItems.add(item);
                    }
                }
            }
            Collections.sort(items);
            Collections.sort(testedItems);
            if ((items.size() == testedItems.size()) && items.equals(testedItems)) {
                if (items.equals(testedItems)) {
                    break;
                }
            }
        }
        missingItems.removeAll(testedItems);
        if (itemsInitialSize != testedItems.size() && missingItems.size() != 0) {
            System.err.println("Failed to confirm all " + itemsInitialSize + boots.size() + " items being generated in " + maxTests + " total generations.");
            System.err.println(testedItems.size() + " items succeeded out of " + itemsInitialSize);
            System.err.println("Tested items: \n" + testedItems.toString());
            System.err.println("Missing items: \n" + missingItems.toString());
            fail();
        } else {
            System.out.println("Successfully confirmed all " + (itemsInitialSize + boots.size()) + " items being generated in " + totalAttempts + " generations.");
            System.out.println("Tested items: \n" + testedItems.toString());
            System.out.println("Missing items: \n" + missingItems.toString());
        }
    }

    @Test
    public void testForKatanasOnHunters() {
        stg.buildType = 0;
        for (int i = 0; i < maxTests; i++) {
            player = stg.makePlayer(stg.getGodFromPosition("hunter"));
            List<String> build = player.getBuild();
            if (build.contains("Hastened Katana") || build.contains("Masamune") || build.contains("Stone Cutting Sword") || build.contains("Golden Blade")) {
                System.err.println("A hunter build contains a forbidden hunter item.");
                System.err.println("Build: " + player.toString());
                fail("A hunter build contains a forbidden hunter item.");
            }
        }
    }

    @Test
    public void testForMultipleMasksOnBuild() {
        for (int i = 0; i < maxTests; i++) {
            player = stg.makePlayer(stg.getGods(1, false).get(0));
            List<String> build = player.getBuild();
            int maskCount = 0;
            for (String j : build) {
                if (j.equals("Bumba's Mask") || j.equals("Lono's Mask") || j.equals("Rangda's Mask")) {
                    maskCount++;
                }
            }
            if (maskCount > 1) {
                fail("There is more than one mask on a build.\nOffending build: " + player.toString());
            }
        }
    }

    @Test
    public void testForMasksOnWrongTypes() {
        stg.buildType = 0;
        for (int i = 0; i < maxTests; i++) {
            player = stg.makePlayer(stg.getGods(1, false).get(0));
            List<String> build = player.getBuild();
            // Rangda's mask on assassins, hunters, or mages
            if ((player.getGod().getPosition().equals("Assassin") || player.getGod().getPosition().equals("Hunter") || player.getGod().getPosition().equals("Mage")) && build.contains("Rangda's Mask")) {
                fail("Rangda's mask is on a " + player.getGod().getPosition() + ".");
            }
            // Lono's mask on warriors or guardians
            else if ((player.getGod().getPosition().equals("Warrior") || player.getGod().getPosition().equals("Guardian")) && build.contains("Lono's Mask")) {
                fail("Lono's mask is on a " + player.getGod().getPosition() + ".");
            }
        }
    }

    @Test
    public void testForItemsNotAvailable() {
        stg.buildType = 0;
        for (int i = 0; i < maxTests; i++) {
            player = stg.makePlayer(stg.getGods(1, false).get(0));
            List<Item> build = player.getBuildAsItems();
            for (Item item : build) {
                if (!item.available(player.getGod())) {
                    fail(item.toString() + " was put on " + player.getGod().getName());
                }
            }
        }
    }

    @Test
    public void getTeamGeneratorStatistics() {
        int testMultiplier = 1;
        class TeamStat {
            public final int teamSize;
            public final long elapsedTime;

            TeamStat(int teamSize, long elapsedTime) {
                this.teamSize = teamSize;
                this.elapsedTime = elapsedTime;
            }
        }
        List<TeamStat> teamStats = new ArrayList<>();
        for (int i = 1; i < 6; i++) {
            long startTime = System.nanoTime();
            System.out.println(i);
            for (int j = 0; j < maxTests * testMultiplier; j++) {
                Team team = stg.generateTeam(i, true, false, 0);
            }
            long endTime = System.nanoTime();
            teamStats.add(new TeamStat(i, endTime - startTime));
        }
        System.out.println("Finished generating " + maxTests * testMultiplier + " teams of each size.");
        long totalElapsed = 0;
        for (TeamStat teamStat : teamStats) {
            totalElapsed += teamStat.elapsedTime;
            System.out.println("Size " + teamStat.teamSize + " | Elapsed time: " + teamStat.elapsedTime / 1000000 + " ms");
        }
        System.out.println("Total time elapsed: " + totalElapsed / 1000000 + " ms or approximately " + totalElapsed / 1000000000 + " seconds.");
    }

    @Test
    public void getGeneratorStatistics() {
        System.out.println("This test is designed to check the percent chance of full builds of a particular play style (Offense, Defense). It will also calculate how fast the generator is on your system. This test will NOT fail.");
        System.out.println("Depending on the 'testMultiplier' value, this test can take anywhere from a few seconds to a few minutes. Just remember, the higher the value for that variable, the more accurate the results are going to be.");
        int testMultiplier = 55;
        System.out.println("'testMultiplier' value is " + testMultiplier);
        int totalAttempts = 0;
        int offensiveTotalOnDefensives = 0;
        int offensiveTotalOnOffensives = 0;
        int defensiveTotalOnDefensives = 0;
        int defensiveTotalOnOffensives = 0;
        stg.buildType = 0;
        // Time values for speed calculation
        long startTime = System.nanoTime();
        List<Long> times = new ArrayList<>();
        for (int i = 0; i < maxTests * testMultiplier; i++) {
            totalAttempts++;
            player = stg.makePlayer(stg.getGods(1, false).get(0));
            boolean defensiveGod = false;
            if (player.getGod().getPosition().equals("Warrior") || player.getGod().getPosition().equals("Guardian")) {
                defensiveGod = true;
            }
            int offensiveItems = 0;
            int defensiveItems = 0;
            for (Item j : player.getBuildAsItems()) {
                if (j.isOffensive() && j.isDefensive()) {
                    offensiveItems++;
                    defensiveItems++;
                } else if (j.isOffensive()) {
                    offensiveItems++;
                } else if (j.isDefensive()) {
                    defensiveItems++;
                }
            }
            if (offensiveItems == 6) {
                if (defensiveGod) {
                    offensiveTotalOnDefensives++;
                } else {
                    offensiveTotalOnOffensives++;
                }
            } else if (defensiveItems == 6) {
                if (defensiveGod) {
                    defensiveTotalOnDefensives++;
                } else {
                    defensiveTotalOnOffensives++;
                }
            }
            if (totalAttempts % 10000 == 0) {
                long currentTime = System.nanoTime();
                long timeElapsed = (currentTime - startTime) / 1000000;
                System.out.println("Generated " + totalAttempts + "/" + testMultiplier * maxTests + " builds. Test is " + ((float) totalAttempts / ((float) testMultiplier * maxTests)) * 100 + "% complete. Time elapsed: " + timeElapsed + "ms");
                times.add(timeElapsed);
                startTime = System.nanoTime();
            }
        }

        // Time calculations
        double avgTime = 0;
        for (Long time : times) {
            avgTime += time;
        }
        avgTime /= times.size();
        double secondsPerInterval = avgTime / 1000;
        double bps = 10000 / secondsPerInterval;

        System.out.println();
        System.out.println("Full Offensive Builds on Offensive Gods Count: " + offensiveTotalOnOffensives + " | Percent Chance: " + (((float) offensiveTotalOnOffensives / (float) totalAttempts) * 100) + "%");
        System.out.println("Full Offensive Builds on Defensive Gods Count: " + offensiveTotalOnDefensives + " | Percent Chance: " + (((float) offensiveTotalOnDefensives / (float) totalAttempts) * 100) + "%");
        System.out.println("Full Defensive Builds on Offensive Gods Count: " + defensiveTotalOnOffensives + " | Percent Chance: " + (((float) defensiveTotalOnOffensives / (float) totalAttempts) * 100) + "%");
        System.out.println("Full Defensive Builds on Defensive Gods Count: " + defensiveTotalOnDefensives + " | Percent Chance: " + (((float) defensiveTotalOnDefensives / (float) totalAttempts) * 100) + "%");
        System.out.println();
        System.out.println("Statistics calculated out of the total " + totalAttempts + " build generations for this particular test.");
        System.out.println("Average time elapsed per 10000 builds: " + avgTime + "ms");
        System.out.println("Calculated builds per second: " + bps + " b/s");
    }

    @Test
    public void testForNotAlwaysBoots() {
        stg.isForcingBoots = false;
        boots.add("Acorn of Yggdrasil");
        boolean noBoots = false;
        int totalNoBoots = 0;
        for (int i = 0; i < maxTests; i++) {
            player = stg.makePlayer(stg.getGods(1, false).get(0));
            List<String> build = player.getBuild();
            if (Collections.disjoint(build, boots)) {
                noBoots = true;
                totalNoBoots++;
            }
        }
        if (!noBoots) {
            fail("Out of the " + maxTests + " tests and with isForcingBoots false, there were never cases of there not being boots. Check the generation algorithm.");
        } else if (totalNoBoots == maxTests) {
            fail("All of the builds were builds without boots. It should not be like this. Check the generation algorithm.");
        } else {
            System.out.println("With isForcingBoots false, there are not always boots in the builds.");
            System.out.println("Out of the " + maxTests + " tests, there were " + totalNoBoots + " builds without boots.");
        }
    }

}
