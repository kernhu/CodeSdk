![image](./static/icon_logo.png)

[![](https://jitpack.io/v/kernhu/CodeSdk.svg)](https://jitpack.io/#kernhu/CodeSdk)
&#x1F353; **CodeSdk** is a kotlin library for scanning and generating QRCode and BarCode fastly and easily. The efficiency and speed more than **Zxing** to scan and analyse the QRCode or Barcode.

&#x1F353; **CodeSdk** 是一个可以快速且便捷的扫描、解析和生成 一维码或二维码的纯kotlin库，扫码和解析出数据的能力和速度远超Zxing。

---------------------------

### Ⅰ：**CodeSdk** includes **code-creator** and **code-scanner**

**code-creator**: generate QRCode or BarCode,it's base **GOOGLE ZXING**(zxing-core-3.5.3) to
compile.

**code-scanner**: scan and parse QRCode or BarCode,it's base **GOOGLE ML-KIT**(barcode-scanning:
17.2.0) to compile.

* advise your project's gradle version is  8.0 and up , gradle plugin version is  8.1.2 and up
* advise your project's kotlin version is 1.8.10 and up.

### Ⅱ: Sample display.

![image](./static/demo-prview.gif)

### Ⅲ: How to use code-scanner?

1.Add it in your root build.gradle at the end of repositories:
```kotlin
	dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url 'https://jitpack.io' }
		}
	}
```

2.add dependence in your build.gradle

```kotlin
dependencies {
   implementation 'com.github.kernhu:CodeSdk:Tag'
}
```

---

3. you can only extend **CodeScannerActivity** or  **CodeScannerFragment**, then override all the
   function . please refer to the class **ScannerActivity** and **ScannerFragment**. such as:

```kotlin
 override fun getHeaderLayoutId(): Int {
    return R.layout.custom_code_scanner_header
}

override fun getFooterLayoutId(): Int {
    return R.layout.custom_code_scanner_footer
}

override fun getSelectAlbumId(): Int {
    return R.id.footer_album
}

override fun getViewfinderColorId(): Int {
    return android.R.color.holo_orange_dark
}

override fun getViewfinderLaserStyle(): ViewfinderView.LaserStyle {
    return ViewfinderView.LaserStyle.GRID
}
override fun onScanResult(content: String) {
    //the result from scanning.
}
```

---

4. if you don't want to extend **CodeScannerActivity** or  **CodeScannerFragment**,you can use **
   CodeScannerView**,please refer to the class **CustomScannerActivity** and **
   CustomScannerFragment**. such as:

```kotlin
<com.xcion.code.scanner.CodeScannerView
android:id = "@+id/code_scanner_view"
android:layout_width = "match_parent"
android:layout_height = "match_parent"
app:csvBeepEnable = "true"
app:csvCodeFormat = "ALL"
app:csvCustomFrameDrawableId = "@drawable/ic_custom_frame"
app:csvCustomLaserDrawableId = "@drawable/ic_custom_laser_line"
app:csvFooterLayoutId = "@layout/custom_code_scanner_footer"
app:csvHeaderLayoutId = "@layout/custom_code_scanner_header"
app:csvSelectAlbumId = "@id/footer_album"
app:csvVibrateEnable = "true"
app:csvViewfinderLaserStyle = "LINE" / >
```

then

```kotlin
/***
 * 1. you must bind the lifecycle of the activity;
 * and register the callback to listen the result value
 * */
binding?.codeScannerView?.addLifecycleObserver(this)?.addCallback(this)?.build(this)


/***
 * 2. you must bind the activity result
 * */
binding?.codeScannerView?.onActivityResult(requestCode, resultCode, data)


/***
 * 3. you must bind the request permission result
 * */
binding?.codeScannerView?.onRequestPermissionsResult(requestCode, permissions, grantResults)


override fun onScanResult(content: String) {
    /***
     *  4. here,you can listen the result of scanner
     * */
}

```

<font color="#dd00dd" size=3 face="Microsoft YaHei">4.Code-Scanner's attribute description.</font>

| Attribute | Method |  English Description | Chinese Description |
| :-----| ----: | ----: | ----: |
| csvBeepEnable | getBeepEnable| whether or not to enable the beep | 是否启用蜂鸣器，默认启用 |
| csvCodeFormat | - |  the code's format,ALL or BARCODE or QRCODE | 支持扫码的格式，BARCODE或QRCOD或ALL(全部)  |
| csvFooterLayoutId | getFooterLayoutId| the resource id of custom footer layout  | 自定义Footer的布局资源id |
| csvHeaderLayoutId | getHeaderLayoutId| the resource id of custom header layout | 自定义Header的布局资源id |
| csvSelectAlbumId | getSelectAlbumId| the view's id for clicking to pick picture in album | 相册选择图片的空间id |
| csvVibrateEnable | getVibrateEnable| whether or not to enable the vibrate |  是否启用震动，默认启用 |
| csvViewfinderColorId | getViewfinderColorId | the viewfinder's theme color | 取景器的主题颜色 |
| csvViewfinderLaserStyle | getViewfinderLaserStyle | the style of viewfinder laser. LINE or GRID  | 取景器雷达扫描线的样式，LINE或GRID |

### Ⅲ: How to use code-creator?

1.add dependence in your build.gradle

```
dependencies {
   implementation 'com.github.kernhu:CodeSdk:Tag'
}
```

2. you can call the class **CodeCreator** ,please refer to the class **CreatorActivity** , such as

```kotlin
var bitmap = BitmapFactory.decodeResource(resources, R.mipmap.icon_logo)
CodeCreator.with()
    .setWidth(600)
    .setHeight(600)
    .setColor(getColor(R.color.purple_200))
    .setLogoSize(100)
    .setLogo(bitmap)
    .setContent(content)
    .setCodeFormat(CodeFormat.QR_CODE)
    .setQRCodeParticle(QRCodeParticle.PIXEL)
    .setCallback(object : CodeCreator.Callback {
        override fun onSuccess(bitmap: Bitmap?) {
            //success
        }

        override fun onFailure(e: Exception?) {
            //fail
        }
    }).generate()
```

<font color="#dd00dd" size=3 face="Microsoft YaHei">3. Code-Creator's attribute description</font>

| Attribute |  English Description | Chinese Description |
| :-----| ----: | ----: |
| setWidth | the code's width | 码的宽 |
| setHeight |  the code's height | 码的高 |
| setColor | the code's foreground color,default BLACK,Takes effect when QRCode | 码的前景色,二维码时生效 |
| setLogoSize | the logo's width and height,Takes effect when QRCode | LOGO的宽高尺寸,二维码时生效 |
| setLogo | logo's bitmap,Takes effect when QRCode | Logo图片,二维码时生效 |
| setContent | the content to generate a code bitmap | 码的内容 |
| setCodeFormat | the code Format(CodeFormat.BAR_CODE or CodeFormat.QR_CODE),Takes effect when QRCode | 码的格式（CodeFormat.BAR_CODE条形码或CodeFormat.QR_CODE二维码）,二维码时生效 |
| setQRCodeParticle | the code Style(QRCodeParticle.PIXEL or QRCodeParticle.DOT),Takes effect when QRCode | 码的样式(QRCodeParticle.PIXEL：像素点 QRCodeParticle.DOT：圆点),二维码时生效 |
| setCallback | the listener to get the result(CodeCreator.Callback) | 回调事件 |

### Ⅳ: Thanks

Thanks for **jenly1314**.The **code-scanner** is base on these library to complete.

[ViewfinderView](https://github.com/jenly1314/ViewfinderView "ViewfinderView")

[CameraScan](https://github.com/jenly1314/CameraScan "CameraScan")

[MLKit](https://github.com/jenly1314/MLKit "MLKit")