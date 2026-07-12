package hauveli.hexagony.features.field

/*
    TODO: decide if I do this after or before mind anchor? this is something that would be same scope as reimplementing fabric carpet players (for the third time for me haha...)
    should be able to:
        query gradient at position
        query accumulated ascent from entity
        place wake to disturb the field (should this be an entity?)
        remove wake to restore the field

        field should take gtp and blink into consideration and obliterate anything travelling through it, somehow.
            how? would I need to mixin to ALL of the things for that?
                I should likely only consider teleportations into, within and from the field, and disregard the rest

        just in general I should figure out how I'm even going to implement this because that's the main hurdle
 */
class Field {
}