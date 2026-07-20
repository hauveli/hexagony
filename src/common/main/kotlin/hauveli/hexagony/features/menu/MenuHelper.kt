package hauveli.hexagony.features.menu


/*
    In order to make this make sense I need to:
        Server->Client "Give me your current screen data"
        Client->Server "Here is my current screen data"
    Any time it changes.
    The problems with this are that this makes a hex not possible to execute in real time if it is server-sided...
    solution:
        sync fucking everything
        bools are 1 bit
        if I only sync once per tick (at most)
        and I only sync when a menu changes
        then it should be fine?
            Can instead sync only on join once,
            then, only when the client state changes
                and if I simulate the rest of the settings, it should be a-okay?
                I don't even need to evaluate the server-side config if so, because I cache whatever the player sends me
                which means clients can have mods which the server doesn't see, and it'll still get to have a copy
                of the same data... sounds ok?
        the server only needs to ask once the player joins, "what are the settings you have access to?"
        and then it can simulate what would happen, based on that information.

 */
object MenuHelper {

}