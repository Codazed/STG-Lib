package com.baconatornoveg.stglib;

import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


public class SmiteTeamGenerator {

    private final List<Item> BOOTS = new ArrayList<>();
    private final List<God> GODS = new ArrayList<>();
    private final List<Item> ITEMS = new ArrayList<>();
    private final List<Item> RELICS = new ArrayList<>();

    public boolean isForcingBalanced = false;
    public boolean isForcingBoots = true;
    public boolean warriorsOffensive = true;

    // Sets what kind of build style to generate on all players
    // 0: default random | 1: attack | 2: defense | 3: half-and-half | 4: Offensive on offensive and defensive on defensive
    public int buildType = 0;

    private Random rand = new Random();

    public final String[] positions = {"assassin", "hunter", "mage", "warrior", "guardian"};

    public String getVersion() {
        return "1.7.0";
    }

    public void getLists(boolean local) {
        Scanner in;
        InputStream bootsFile = null;
        InputStream godsFile = null;
        InputStream itemsFile = null;
        InputStream relicsFile = null;
        boolean useLocal = local;
        if (!local) {
            System.out.println("Attempting to fetch the current lists from GitLab...");
            try {
                String bootsRemoteUrl = "https://gitlab.com/Codazed/STG-Lib/raw/master/Lists/boots.csv";
                URL bootsUrl = new URL(bootsRemoteUrl);
                String godsRemoteUrl = "https://gitlab.com/Codazed/STG-Lib/raw/master/Lists/gods.csv";
                URL godsUrl = new URL(godsRemoteUrl);
                String itemsRemoteUrl = "https://gitlab.com/Codazed/STG-Lib/raw/master/Lists/items.csv";
                URL itemsUrl = new URL(itemsRemoteUrl);
                String relicsRemoteUrl = "https://gitlab.com/Codazed/STG-Lib/raw/master/Lists/relics.csv";
                URL relicsUrl = new URL(relicsRemoteUrl);
                bootsFile = bootsUrl.openStream();
                godsFile = godsUrl.openStream();
                itemsFile = itemsUrl.openStream();
                relicsFile = relicsUrl.openStream();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Failed to get the lists from GitLab. Falling back to compiled lists.");
                useLocal = true;
            }
        }

        if (local || useLocal) {
            System.out.println("Using the compiled lists, these could be out of date...");
            try {
                bootsFile = getClass().getResourceAsStream("/boots.csv");
                godsFile = getClass().getResourceAsStream("/gods.csv");
                itemsFile = getClass().getResourceAsStream("/items.csv");
                relicsFile = getClass().getResourceAsStream("/relics.csv");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Begin parsing boots
        in = new Scanner(bootsFile);

        in.nextLine();

        while (in.hasNextLine()) {
            String line = in.nextLine();
            String[] values = line.split(",");
            String[] availability = {values[1], values[1], values[2], values[1], values[2]};
            BOOTS.add(new Item(values[0], values[1], values[2], values[3], availability));
        }
        in.close();
        in = new Scanner(godsFile);
        in.nextLine();

        while (in.hasNextLine()) {
            String line = in.nextLine();
            String[] values = line.split(",");
            GODS.add(new God(values[0], values[1]));
        }
        in.close();
        in = new Scanner(itemsFile);
        in.nextLine();

        while (in.hasNextLine()) {
            String line = in.nextLine();
            String[] values = line.split(",");
            String[] availability = {values[4], values[5], values[6], values[7], values[8]};
            ITEMS.add(new Item(values[0], values[1], values[2], values[3], availability));
        }
        in.close();

        in = new Scanner(relicsFile);
        in.nextLine();
        while (in.hasNextLine()) {
            String line = in.nextLine();
            String[] values = line.split(",");
            String[] availability = {"TRUE", "TRUE", "TRUE", "TRUE", "TRUE"};
            RELICS.add(new Item(values[0], "TRUE", "TRUE", "Relic", availability));
        }
        in.close();
        System.out.println("STG-Lib successfully loaded " + BOOTS.size() + " boots, " + GODS.size() + " gods, " + RELICS.size() + " relics, and " + ITEMS.size() + " items.");
    }

    // Implementing Fisher–Yates shuffle
    private static void shufflePositions(String[] ar) {
        // If running on Java 6 or older, use `new Random()` on RHS here
        Random rnd = ThreadLocalRandom.current();
        for (int i = ar.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            String a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    public Team generateTeam(int size, boolean forceBalanced, boolean forceBoots, int buildType) {
        isForcingBalanced = forceBalanced;
        isForcingBoots = forceBoots;
        this.buildType = buildType;
        List<God> gods = getGods(size, forceBalanced);
        Team team = new Team(this);
        for (God god : gods) {
            Player player = makePlayer(god);
            team.add(player);
        }
        /*if (forceBalanced) {
            // Generate a team that does not duplicate positions
            shufflePositions(positions);
            for (int i = 0; i < size; i++) {
                Player loadout = makePlayer(positions[i]);
                team.add(loadout);
            }

        } else {
            // Generate a team that can have more than one god of the same position
            for (int i = 0; i < size; i++) {
                Player loadout = makePlayer(positions[(int) (Math.random() * (positions.length))]);
                team.add(loadout);
            }
            boolean dupes = true;
            while (dupes) {
                dupes = false;
                for (int i = 0; i < size; i++) {
                    Player currentPlayer = team.getPlayer(i);
                    for (int j = 0; j < size; j++) {
                        if ((i != j) && currentPlayer.getGod().equals(team.getPlayer(j).getGod())) {
                            dupes = true;
                            team.set(j, makePlayer(positions[(int) (Math.random() * (positions.length))]));
                            break;
                        }
                    }
                    if (dupes) {
                        break;
                    }
                }
            }
        }*/
        return team;
    }

    public Player makePlayer(God god) {
        List<Item> playerBuild = null;
        List<Item> build;
        List<Item> relics = new ArrayList<>();
        //god = getGod(position);
        build = getItems(god);
        //playerBuild = processBuild(god, build);
        playerBuild = build;

        Item firstRelic;
        Item secondRelic;
        firstRelic = getRelic();
        secondRelic = getRelic();
        while (firstRelic.equals(secondRelic)) {
            secondRelic = getRelic();
        }
        relics.add(firstRelic);
        relics.add(secondRelic);
        return new Player(god, playerBuild, relics);
    }

    private boolean checkMasks(List<Item> build) {
        int numMasks = 0;
        for (Item item : build) {
            if (item.isMask()) {
                numMasks++;
                if (numMasks > 1) {
                    return false;
                }
            }
        }
        return true;
    }

    private List<Item> processBuild(God god, List<Item> items) {
        List<Item> build = items;
        List<Item> playerBuild;
        while (true) {
            if (!god.checkBuild(build)) {
                build = getItems(god);
            } else if (!checkMasks(build)) {
                build = getItems(god);
            } else {
                boolean buildReady = false;
                int offensiveCount = 0;
                int defensiveCount = 0;
                for (Item i : build) {
                    if (i.isOffensive() && i.isDefensive()) {
                        offensiveCount++;
                        defensiveCount++;
                    } else if (i.isOffensive()) offensiveCount++;
                    else if (i.isDefensive()) defensiveCount++;
                }
                switch (buildType) {
                    default:
                    case 0:
                        // Default random
                        buildReady = true;
                        break;
                    case 1:
                        // Full offensive
                        if (offensiveCount < 6) build = getItems(god);
                        else buildReady = true;
                        break;
                    case 2:
                        // Full defensive
                        if (defensiveCount < 6) build = getItems(god);
                        else buildReady = true;
                        break;
                    case 3:
                        // Half-and-half
                        if (offensiveCount < 3 || defensiveCount < 3) build = getItems(god);
                        else buildReady = true;
                        break;
                    case 4:
                        // Equivalent to isForcingOffensive and isForcingDefensive both true
                        if ("Assassin".equalsIgnoreCase(god.getPosition()) || "Hunter".equalsIgnoreCase(god.getPosition()) || "Mage".equalsIgnoreCase(god.getPosition()) || ("Warrior".equalsIgnoreCase(god.getPosition()) && warriorsOffensive)) {
                            if (offensiveCount < 6) build = getItems(god);
                            else buildReady = true;
                        } else {
                            if (defensiveCount < 6) build = getItems(god);
                            else buildReady = true;
                        }
                        break;
                }
                if (buildReady) break;
            }
        }
        playerBuild = build;
        return playerBuild;
    }

    /*private List<Item> generateBuild(God god) {
        List<Item> items = new ArrayList<>();
        //LinkedHashSet<Item> generation = new LinkedHashSet<>();
        String type = god.getPosition();
        Item newItem;
        if (type.matches("Assassin|Hunter|Warrior")) {
            if ("Ratatoskr".equals(god.getName())) {
                String[] availability = {"TRUE", "FALSE", "FALSE", "FALSE", "FALSE"};
                items.add(new Item("Acorn of Yggdrasil", "true", "false", "BOTH", availability));
            } else {
                if ((int) (Math.random() * 100) > 35 && !isForcingBoots) {
                    generation.add(getItem("physical"));
                } else {
                    generation.add(getBoot(god));
                }
            }
            for (int i = 0; i < 5; i++) {
                newItem = getItem("physical");
                generation.add(newItem);
            }

            while (generation.size() < 6) {
                newItem = getItem("physical");
                generation.add(newItem);
            }

        } else {
            if ((int) (Math.random() * 100) > 35 && !isForcingBoots) {
                generation.add(getItem("magical"));
            } else {
                generation.add(getBoot(god));
            }
            for (int i = 0; i < 5; i++) {
                newItem = getItem("magical");
                generation.add(newItem);
            }

            while (generation.size() < 6) {
                newItem = getItem("magical");
                generation.add(newItem);
            }
        }
        items.addAll(0, generation);
        return items;
    }*/

    private Item getBoot(God god) {
        Item boot;
        boot = BOOTS.get((int) (Math.random() * BOOTS.size()));
        if (god.isPhysical()) {
            while (boot.isMagical()) {
                boot = BOOTS.get((int) (Math.random() * BOOTS.size()));
            }
        } else {
            while (boot.isPhysical()) {
                boot = BOOTS.get((int) (Math.random() * BOOTS.size()));
            }
        }
        return boot;
    }

    private Item getRelic() {
        Item relic;
        relic = RELICS.get((int) (Math.random() * RELICS.size()));
        return relic;
    }

    private Item getItem(String type) {
        Item item;
        boolean physical = type.equalsIgnoreCase("physical");
        item = ITEMS.get((int) (Math.random() * (ITEMS.size())));
        if (physical) {
            while (item.isMagical() && !item.isPhysical()) {
                item = ITEMS.get((int) (Math.random() * (ITEMS.size())));
            }
        } else {
            while (item.isPhysical() && !item.isMagical()) {
                item = ITEMS.get((int) (Math.random() * (ITEMS.size())));
            }
        }
        return item;
    }

    private List<Item> getItems(God god) {
        boolean forceBoot = isForcingBoots;
        List<Item> availableItems = new ArrayList<>(ITEMS);
        List<Item> availableBoots = new ArrayList<>(BOOTS);
        List<Item> returnList = new ArrayList<>();
        availableBoots.removeIf(boot -> !boot.available(god));
        if ("Ratatoskr".equals(god.getName())) {
            if (forceBoot) {
                String[] availability = {"TRUE", "FALSE", "FALSE", "FALSE", "FALSE"};
                returnList.add(new Item("Acorn of Yggdrasil", "true", "false", "BOTH", availability));
            }
        } else if (forceBoot) {
            Item newBoot = availableBoots.remove((int) (Math.random() * availableBoots.size()));
            returnList.add(newBoot);
        } else {
            availableItems.addAll(availableBoots);
        }
        while(returnList.size() < 6) {
            Item newItem = availableItems.remove((int) (Math.random() * availableItems.size()));
            if (newItem.isMask()) {
                availableItems.removeIf(Item::isMask);
            } else if (BOOTS.contains(newItem)) {
                availableItems.removeIf(BOOTS::contains);
            }
            returnList.add(newItem);
        }
        return returnList;
    }

    public List<God> getGods(int num, boolean uniquePositions) {
        List<God> availableGods = new ArrayList<>(GODS);
        List<God> returnList = new ArrayList<>();
        for(int i = 0; i < num; i++) {
            God newGod = availableGods.remove((int) (Math.random() * availableGods.size()));
            if (uniquePositions) {
                String position = newGod.getPosition();
                availableGods.removeIf(god -> position.equals(god.getPosition()));
            }
            returnList.add(newGod);
        }
        return returnList;
    }

    public God getGodFromPosition(String position) {
        List<God> availableGods = GODS;
        availableGods.removeIf(god -> !position.equalsIgnoreCase(god.getPosition()));
        return availableGods.get((int) (Math.random() * (availableGods.size())));
    }

    public List<Item> getBOOTS() {
        return BOOTS;
    }

    public List<Item> getITEMS() {
        return ITEMS;
    }

    public List<God> getGODS() {
        return GODS;
    }

    public List<String> getGodsAsStrings() {
        List<String> returnList = new ArrayList<>();
        for (God i : GODS) {
            returnList.add(i.getName());
        }
        return returnList;
    }

    public List<String> getItemsAsStrings() {
        List<String> returnList = new ArrayList<>();
        for (Item i : ITEMS) {
            returnList.add(i.toString());
        }
        return returnList;
    }

    public List<String> getBootsAsStrings() {
        List<String> returnList = new ArrayList<>();
        for (Item i : BOOTS) {
            returnList.add(i.toString());
        }
        return returnList;
    }
}