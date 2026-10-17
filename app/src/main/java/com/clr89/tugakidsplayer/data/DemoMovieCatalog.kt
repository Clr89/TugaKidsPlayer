package com.clr89.tugakidsplayer.data

import com.clr89.tugakidsplayer.data.model.Movie

private const val DEMO_VIDEO_URL =
    "https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4"

/** Local sample data kept outside the UI so it can later be replaced by a repository. */
object DemoMovieCatalog {
    val movies = listOf(
        // All sample titles share one openly licensed short clip until real catalogue media is configured.
        Movie("moon-garden", "O Jardim da Lua", "https://picsum.photos/seed/moon-garden/640/360", DEMO_VIDEO_URL),
        Movie("cloud-train", "O Comboio das Nuvens", "https://picsum.photos/seed/cloud-train/640/360", DEMO_VIDEO_URL),
        Movie("blue-planet", "O Planeta Azul", "https://picsum.photos/seed/blue-planet/640/360", DEMO_VIDEO_URL),
        Movie("forest-band", "A Banda da Floresta", "https://picsum.photos/seed/forest-band/640/360", DEMO_VIDEO_URL),
        Movie("little-astronaut", "O Pequeno Astronauta", "https://picsum.photos/seed/little-astronaut/640/360", DEMO_VIDEO_URL),
        Movie("island-treasure", "O Tesouro da Ilha", "https://picsum.photos/seed/island-treasure/640/360", DEMO_VIDEO_URL),
        Movie("winter-kite", "O Papagaio de Inverno", "https://picsum.photos/seed/winter-kite/640/360", DEMO_VIDEO_URL),
        Movie("robot-friend", "O Meu Amigo Robô", "https://picsum.photos/seed/robot-friend/640/360", DEMO_VIDEO_URL),
    )
}
