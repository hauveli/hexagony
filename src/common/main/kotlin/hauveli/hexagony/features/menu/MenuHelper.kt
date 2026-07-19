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
        the server only needs to ask once the player joins, "what are the settings you have access to?"
        and then it can simulate what would happen, based on that information.
 */
object MenuHelper {

}