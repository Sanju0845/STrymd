package com.example.data.mediastore

import com.example.domain.model.Song

object DemoMusicProvider {

    fun getDemoSongs(): List<Song> {
        return listOf(
            Song(
                id = -101L,
                title = "Midnight Horizon",
                artist = "Solaris Echo",
                album = "Neon Drift",
                durationMs = 214000L,
                contentUri = "https://cdn.pixabay.com/download/audio/2022/05/27/audio_1808fbf07a.mp3?filename=midnight-forest-184304.mp3",
                albumArtUri = "https://picsum.photos/seed/aurasong1/600/600",
                albumId = -1L,
                artistId = -1L,
                trackNumber = 1,
                year = 2024,
                genre = "Synthwave",
                folderName = "Music",
                dateAdded = System.currentTimeMillis() / 1000 - 86400,
                lyrics = """
                    [00:12.00]Glow of the city under violet skies
                    [00:18.50]Chasing reflections in your electric eyes
                    [00:26.00]Speeding through memories carved in the dark
                    [00:33.20]Leaving behind every fading spark
                    [00:41.00]We're caught in the midnight horizon
                    [00:48.00]Where time is an illusion we run from
                    [00:55.00]Feel the frequency rising inside
                    [01:03.00]Nowhere to run and nowhere to hide
                """.trimIndent()
            ),
            Song(
                id = -102L,
                title = "Liquid Glass",
                artist = "Aura Collective",
                album = "Reflections in Prism",
                durationMs = 188000L,
                contentUri = "https://cdn.pixabay.com/download/audio/2022/01/18/audio_d0a13f69d2.mp3?filename=ambient-piano-amp-strings-10711.mp3",
                albumArtUri = "https://picsum.photos/seed/aurasong2/600/600",
                albumId = -2L,
                artistId = -2L,
                trackNumber = 2,
                year = 2024,
                genre = "Ambient",
                folderName = "Downloads",
                dateAdded = System.currentTimeMillis() / 1000 - 172800,
                lyrics = """
                    [00:08.00]Drifting through translucence
                    [00:15.00]Shapes that shift and sway
                    [00:22.00]Light across the prism
                    [00:30.00]Washing fears away
                    [00:42.00]Pure liquid glass
                    [00:50.00]Stillness in the sound
                    [01:02.00]Echoes all around
                """.trimIndent()
            ),
            Song(
                id = -103L,
                title = "Hyperdrive Groove",
                artist = "Kavinsky Lane",
                album = "Neon Drift",
                durationMs = 245000L,
                contentUri = "https://cdn.pixabay.com/download/audio/2022/10/14/audio_9939f792cb.mp3?filename=electronic-future-beats-117997.mp3",
                albumArtUri = "https://picsum.photos/seed/aurasong3/600/600",
                albumId = -1L,
                artistId = -3L,
                trackNumber = 3,
                year = 2024,
                genre = "Electronic",
                folderName = "Music",
                dateAdded = System.currentTimeMillis() / 1000 - 250000,
                lyrics = """
                    [00:10.00]Ignition countdown three two one
                    [00:16.00]Racing into the digital sun
                    [00:24.00]Bassline shaking the stellar floor
                    [00:32.00]Step through the hyperdrive door
                    [00:44.00]Move with the groove, let it take control
                    [00:52.00]Analog warmth in a cyber soul
                """.trimIndent()
            ),
            Song(
                id = -104L,
                title = "Starlit Acoustic",
                artist = "Elena Vance",
                album = "Quiet Valleys",
                durationMs = 195000L,
                contentUri = "https://cdn.pixabay.com/download/audio/2022/03/15/audio_c8c8a73467.mp3?filename=acoustic-guitar-relax-music-110757.mp3",
                albumArtUri = "https://picsum.photos/seed/aurasong4/600/600",
                albumId = -4L,
                artistId = -4L,
                trackNumber = 1,
                year = 2023,
                genre = "Acoustic",
                folderName = "WhatsApp Audio",
                dateAdded = System.currentTimeMillis() / 1000 - 400000,
                lyrics = """
                    [00:14.00]Campsite embers glowing red
                    [00:21.00]Words that remain unsaid
                    [00:29.00]Gentle breeze through the pine
                    [00:37.00]Your hand holding mine
                    [00:48.00]Underneath the starlit sky
                    [00:56.00]Watching satellites pass by
                """.trimIndent()
            ),
            Song(
                id = -105L,
                title = "Velvet Sunset",
                artist = "Solaris Echo",
                album = "Golden Hour Sessions",
                durationMs = 230000L,
                contentUri = "https://cdn.pixabay.com/download/audio/2021/08/04/audio_bb630cc098.mp3?filename=lofi-study-112191.mp3",
                albumArtUri = "https://picsum.photos/seed/aurasong5/600/600",
                albumId = -5L,
                artistId = -1L,
                trackNumber = 2,
                year = 2024,
                genre = "Lo-Fi",
                folderName = "Music",
                dateAdded = System.currentTimeMillis() / 1000 - 500000,
                lyrics = """
                    [00:15.00]Amber rays across the bay
                    [00:23.00]Saying goodbye to another day
                    [00:32.00]Smooth chords soothing the mind
                    [00:41.00]Leave the noisy world behind
                    [00:50.00]Velvet warmth, gentle and slow
                    [00:58.00]Bask in the sunset glow
                """.trimIndent()
            )
        )
    }
}
