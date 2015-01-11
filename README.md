# Video.js Java

Video.js Java Tech plug-in

A Video.js tech plugin that add AIFF, AVI, GSM, MID, MPG, MP2, MOV, AU and WAV stream support through Java.

## Getting Started

Once you've added the plugin script to your page, you can use it with any supported video:
 * Include JavaScript files
```html
<script src="video.js"></script>
<script src="videojs-java.js"></script>
```
 * And add this new tech to the player:
```html
data-setup='{ "techOrder": ["java"] }'
```

There's also a [working example](example.html) of the plugin you can check out if you're having trouble.

## Documentation
### Plugin Options

This plugin has a global configuration to setup JAR file location.
```html
<script>
    videojs.options.java.jar = "video-js.jar";
</script>
```

## Release History

 - 0.1.0: Initial release
