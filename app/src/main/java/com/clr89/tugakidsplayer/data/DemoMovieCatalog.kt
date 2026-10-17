package com.clr89.tugakidsplayer.data

import com.clr89.tugakidsplayer.data.model.Movie

/** Local sample data kept outside the UI so it can later be replaced by a repository. */
object DemoMovieCatalog {
    val movies = listOf(
        Movie("moon-garden", "O Jardim da Lua", "https://picsum.photos/seed/moon-garden/640/360", "https://example.com/videos/moon-garden.mp4"),
        Movie("cloud-train", "O Comboio das Nuvens", "https://picsum.photos/seed/cloud-train/640/360", "https://example.com/videos/cloud-train.mp4"),
        Movie("blue-planet", "O Planeta Azul", "https://picsum.photos/seed/blue-planet/640/360", "https://example.com/videos/blue-planet.mp4"),
        Movie("forest-band", "A Banda da Floresta", "https://picsum.photos/seed/forest-band/640/360", "https://example.com/videos/forest-band.mp4"),
        Movie("little-astronaut", "O Pequeno Astronauta", "https://picsum.photos/seed/little-astronaut/640/360", "https://example.com/videos/little-astronaut.mp4"),
        Movie("island-treasure", "O Tesouro da Ilha", "https://picsum.photos/seed/island-treasure/640/360", "https://example.com/videos/island-treasure.mp4"),
        Movie("winter-kite", "O Papagaio de Inverno", "https://picsum.photos/seed/winter-kite/640/360", "https://example.com/videos/winter-kite.mp4"),
        Movie("robot-friend", "O Meu Amigo Robô", "https://picsum.photos/seed/robot-friend/640/360", "https://example.com/videos/robot-friend.mp4"),
    )
}
