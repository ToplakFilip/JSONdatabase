JSON database which uses multithreading processing for multiple requests at the same time.
It uses GSON for serialization/deserialization of json objects and Jcommander for command line arguments.

<pre>
-t set -k vegetables -v carrot
</pre>
Command line argument structure consisting of -t(request name), -k(JSON key), -v(JSON value)

Defined in Args.java

<pre>
-in getFile.json
</pre>
Requests commands from a file inside "data" folder with argument -in

<pre>
{"type":"delete","key":["fruit","grapes","rainbow_grapes"]}
</pre>
used for manipulating nested objects by having a list key.

<pre>
{
  "type": "set",
  "key": "vegetables",
  "value": {
    "carrot": {
      "size": "medium",
      "rarity": "common",
      "color": "orange"
    },
    "cabbage": {
      "size": "big",
      "rarity": "common",
      "types": {
        "green_cabbage": {
          "color": "green",
          "taste": "sour"
        },
        "red_cabbage": {
          "color": "purple",
          "taste": "bitter"
        }
      }
    }
  }
}
</pre>
Set type parameter can add entire JSON file with nested objects with value parameter.