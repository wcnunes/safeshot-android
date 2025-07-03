package com.seuprojeto.safeshot

import androidx.exifinterface.media.ExifInterface

object MetadataUtils {
    fun getMetadataString(exif: ExifInterface): String {
        val tags = listOf(
            ExifInterface.TAG_MAKE,
            ExifInterface.TAG_MODEL,
            ExifInterface.TAG_DATETIME,
            ExifInterface.TAG_GPS_LATITUDE,
            ExifInterface.TAG_GPS_LONGITUDE,
            ExifInterface.TAG_USER_COMMENT
        )
        return tags.joinToString("\n") { tag ->
            val value = exif.getAttribute(tag)
            if (value != null) "$tag: $value" else ""
        }.trim()
    }

    fun cleanMetadata(exif: ExifInterface) {
        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, null)
        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, null)
        exif.setAttribute(ExifInterface.TAG_USER_COMMENT, null)
        exif.setAttribute(ExifInterface.TAG_DATETIME, null)
        exif.setAttribute(ExifInterface.TAG_MAKE, null)
        exif.setAttribute(ExifInterface.TAG_MODEL, null)
        exif.saveAttributes()
    }
} 