package hauveli.hexagony.features.field

/*
    probably after... this is a feature that in terms of effort to conceptualize a feasible implementation is equivalent to the entire project combined, or more
    my best idea isn't even that good thus far, but it would be performant, just a little abstract and difficult to imagine all at once...
    TODO: decide if I do this after or before mind anchor? this is something that would be same scope as reimplementing fabric carpet players (for the third time for me haha...)
    should be able to:
        query gradient at position
        query accumulated ascent from entity
        place wake to disturb the field (should this be an entity?)
        remove wake to restore the field
        wake should decay after some amount of ticks, this makes cleaning up simpler
            could limit number of wakes to something like 1000, and check which chunk a wake is in to determine if it should be evaluated
            same chunk -> check? this would cause strange behavior near corners... checking chunks bordering within 4 blocks of the player for wakes might be good?

        alternatively, I could just use the physics-based simulation I wrote ages ago for this, it scales awfully, but locally it's good
            pros: free visualization
            cons: awful performance

        field should take gtp and blink into consideration and obliterate anything travelling through it, somehow.
            how? would I need to mixin to ALL of the things for that?
                I should likely only consider teleportations into, within and from the field, and disregard the rest

        just in general I should figure out how I'm even going to implement this because that's the main hurdle


 */
class Field {
}