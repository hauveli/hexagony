# Hexagony

[![powered by hexdoc](https://img.shields.io/endpoint?url=https://hexxy.media/api/v0/badge/hexdoc?label=1)](https://github.com/hexdoc-dev/hexdoc)

Hexagony addon for Hex Casting

For gating great spells and having them show up in the hexbook with their patterns when unlocked, please add an advancement:
```
{
  "criteria": {
    "unlock": {
      "conditions": {
        "pattern": "yournamespace:yourgreatspell"
      },
      "trigger": "hexagony:has_held_pattern"
    }
  },
  "requirements": [
    [
      "unlock"
    ]
  ],
  "sends_telemetry_event": true
}
```
(I'll get around to adding a better way to do this later...)


All .ogg files under the [selulance](src/common/main/resources/assets/hexagony/sounds/music_disc/selulance/) directory are compressed versions of music made by and belonging to Selulance, who may be found on bandcamp here: https://selulance.bandcamp.com/music or soundcloud here: https://soundcloud.com/selulance, under the [Creative Commons - Attribution 3.0](src/common/main/resources/assets/hexagony/sounds/music_disc/selulance/LICENSE) license.