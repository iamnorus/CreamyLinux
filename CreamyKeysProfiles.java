package com.creamykeys.linux;

import java.util.List;

final class CreamyKeysProfiles {
    static final List<String> KEYBOARDS = List.of(
            "cherrymx_black_abs",
            "cherrymx_black_pbt",
            "cherrymx_blue_abs",
            "cherrymx_blue_abs_2",
            "cherrymx_brown_abs",
            "cherrymx_brown_pbt",
            "cherrymx_red_abs",
            "custom"
    );
    static final List<String> CLICKS = List.of(
            "same_as_keyboard", "random", "hit", "hit1", "hit2", "hit3", "hit4",
            "laser", "ponk", "custom", "corsair"
    );
    static final List<String> SOUND_IDS = List.of(
            "hit", "hit1", "hit2", "hit3", "hit4", "laser", "ponk", "custom",
            "corsair_left", "corsair_middle"
    );
    static final List<String> RANDOM_CLICKS = List.of(
            "hit", "hit1", "hit2", "hit3", "hit4", "laser", "ponk"
    );

    private CreamyKeysProfiles() {
    }

    static String clickSoundName(String profile, int button) {
        return switch (profile) {
            case "random" -> RANDOM_CLICKS.get(
                    java.util.concurrent.ThreadLocalRandom.current().nextInt(RANDOM_CLICKS.size())
            );
            case "corsair" -> switch (button) {
                case 0 -> "corsair_left";
                case 2 -> "corsair_middle";
                default -> "hit";
            };
            default -> profile;
        };
    }
}
