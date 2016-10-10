package net.gini.android.vision.internal.camera.photo;

import android.os.Build;
import android.support.annotation.NonNull;

import net.gini.android.vision.BuildConfig;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffDirectoryType;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.fieldtypes.FieldType;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputField;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @exclude
 */
public class Exif {

    private final TiffOutputSet mTiffOutputSet;

    private Exif(@NonNull TiffOutputSet tiffOutputSet) {
        mTiffOutputSet = tiffOutputSet;
    }

    @NonNull
    public static Builder builder() throws ImageWriteException {
        return new Builder();
    }

    @NonNull
    public byte[] writeToJpeg(@NonNull byte[] jpeg) throws ImageWriteException, ImageReadException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ExifRewriter exifRewriter = new ExifRewriter();
        exifRewriter.updateExifMetadataLossless(jpeg, outputStream, mTiffOutputSet);

        return outputStream.toByteArray();
    }

    @NonNull
    public static RequiredTags readRequiredTags(@NonNull byte[] jpeg) throws IOException, ImageReadException {
        RequiredTags requiredTags = new RequiredTags();

        JpegImageMetadata jpegMetadata = (JpegImageMetadata) Imaging.getMetadata(jpeg);

        if (jpegMetadata != null) {
            requiredTags.make = jpegMetadata.findEXIFValue(TiffTagConstants.TIFF_TAG_MAKE);
            requiredTags.model = jpegMetadata.findEXIFValue(TiffTagConstants.TIFF_TAG_MODEL);
            requiredTags.iso = jpegMetadata.findEXIFValue(ExifTagConstants.EXIF_TAG_ISO);
            requiredTags.exposure = jpegMetadata.findEXIFValue(ExifTagConstants.EXIF_TAG_EXPOSURE_TIME);
            requiredTags.aperture = jpegMetadata.findEXIFValue(ExifTagConstants.EXIF_TAG_APERTURE_VALUE);
            requiredTags.flash = jpegMetadata.findEXIFValue(ExifTagConstants.EXIF_TAG_FLASH);
            requiredTags.compressedBitsPerPixel = jpegMetadata.findEXIFValue(ExifTagConstants.EXIF_TAG_COMPRESSED_BITS_PER_PIXEL);
        }

        return requiredTags;
    }

    public static class Builder {

        private TiffOutputSet mTiffOutputSet;
        private TiffOutputDirectory mIfd0Directory;
        private TiffOutputDirectory mExifDirectory;

        private Builder() throws ImageWriteException {
            // Create a new exif metadata set, to keep only the required exif tags
            mTiffOutputSet = new TiffOutputSet();

            mExifDirectory = mTiffOutputSet.getOrCreateExifDirectory();
            mIfd0Directory = mTiffOutputSet.findDirectory(TiffDirectoryType.TIFF_DIRECTORY_IFD0.directoryType);
            if (mIfd0Directory == null) {
                mIfd0Directory = new TiffOutputDirectory(TiffDirectoryType.TIFF_DIRECTORY_IFD0.directoryType, mTiffOutputSet.byteOrder);
                mTiffOutputSet.addDirectory(mIfd0Directory);
            }
        }

        @NonNull
        public Builder setRequiredTags(@NonNull RequiredTags requiredTags) throws ImageReadException, ImageWriteException {
            // Make
            if (requiredTags.make != null) {
                addStringExif(mIfd0Directory, requiredTags.make);
            }
            // Model
            if (requiredTags.model != null) {
                addStringExif(mIfd0Directory, requiredTags.model);
            }
            // ISO
            if (requiredTags.iso != null) {
                try {
                    TiffOutputField isoField = createTiffOutputField(requiredTags.iso);
                    mExifDirectory.add(isoField);
                } catch (Exception e) {
                    // Ignore, ClassCastException was thrown on a Galaxy Nexus w. Android 4.3
                }
            }
            // Exposure
            if (requiredTags.exposure != null) {
                try {
                    TiffOutputField exposureField = createTiffOutputField(requiredTags.exposure);
                    mExifDirectory.add(exposureField);
                } catch (Exception e) {
                    // Shouldn't happen, but ignore it, if it does
                }
            }
            // Aperture
            if (requiredTags.aperture != null) {
                try {
                    TiffOutputField apertureField = createTiffOutputField(requiredTags.aperture);
                    mExifDirectory.add(apertureField);
                } catch (Exception e) {
                    // Shouldn't happen, but ignore it, if it does
                }
            }
            // Flash
            if (requiredTags.flash != null) {
                try {
                    TiffOutputField flashField = createTiffOutputField(requiredTags.flash);
                    mExifDirectory.add(flashField);
                } catch (Exception e) {
                    // Shouldn't happen, but ignore it, if it does
                }
            }
            // Compressed bits per pixel
            if (requiredTags.compressedBitsPerPixel != null) {
                try {
                    TiffOutputField compressedBitsPerPixelField = createTiffOutputField(requiredTags.compressedBitsPerPixel);
                    mExifDirectory.add(compressedBitsPerPixelField);
                } catch (Exception e) {
                    // Shouldn't happen, but ignore it, if it does
                }
            }
            return this;
        }

        @NonNull
        private TiffOutputField createTiffOutputField(@NonNull TiffField tiffField) {
            return new TiffOutputField(tiffField.getTagInfo(),
                    tiffField.getFieldType(),
                    (int) tiffField.getCount(),
                    tiffField.getByteArrayValue());
        }

        @NonNull
        public Builder setUserComment(boolean addMake, boolean addModel) {
            addUserCommentStringExif(mExifDirectory, createUserComment(addMake, addModel));
            return this;
        }

        @NonNull
        public Builder setOrientationFromDegrees(int degrees) {
            byte[] bytes = new byte[1];
            bytes[0] = (byte) rotationToExifOrientation(degrees);
            TiffOutputField orientationOutputField = new TiffOutputField(TiffTagConstants.TIFF_TAG_ORIENTATION, FieldType.SHORT, 1, bytes);
            mIfd0Directory.add(orientationOutputField);
            return this;
        }

        @NonNull
        public Exif build() {
            return new Exif(mTiffOutputSet);
        }

        private void addStringExif(@NonNull TiffOutputDirectory outputDirectory, @NonNull TiffField field) throws ImageReadException {
            byte bytes[] = field.getStringValue().getBytes(Charset.forName("US-ASCII"));
            addStringExif(outputDirectory, field.getTagInfo(), bytes);
        }

        private void addUserCommentStringExif(@NonNull TiffOutputDirectory outputDirectory, @NonNull String value) {
            // ASCII character code
            byte characterCode[] = new byte[]{0x41, 0x53, 0x43, 0x49, 0x49, 0x00, 0x00, 0x00};

            byte comment[] = value.getBytes(Charset.forName("US-ASCII"));
            byte userComment[] = new byte[characterCode.length + comment.length];

            System.arraycopy(characterCode, 0, userComment, 0, characterCode.length);
            System.arraycopy(comment, 0, userComment, characterCode.length, comment.length);

            addStringExif(outputDirectory, ExifTagConstants.EXIF_TAG_USER_COMMENT, userComment);
        }

        private void addStringExif(TiffOutputDirectory outputDirectory, TagInfo tagInfo, byte[] bytes) {
            TiffOutputField outputField = new TiffOutputField(tagInfo, FieldType.ASCII, bytes.length, bytes);
            outputDirectory.add(outputField);
        }

        @NonNull
        private String createUserComment(boolean addMake, boolean addModel) {
            StringBuilder userCommentBuilder = new StringBuilder();
            // Make
            if (addMake) {
                userCommentBuilder.append("Make=");
                userCommentBuilder.append(Build.BRAND);
                userCommentBuilder.append(",");
            }
            // Model
            if (addModel) {
                userCommentBuilder.append("Model=");
                userCommentBuilder.append(Build.MODEL);
                userCommentBuilder.append(",");
            }
            // Platform
            userCommentBuilder.append("Platform=Android");
            userCommentBuilder.append(",");
            // OS Version
            userCommentBuilder.append("OSVer=");
            userCommentBuilder.append(String.valueOf(Build.VERSION.RELEASE));
            userCommentBuilder.append(",");
            // GiniVision Version
            userCommentBuilder.append("GiniVisionVer=");
            userCommentBuilder.append(BuildConfig.VERSION_NAME.replace(" ", ""));

            return userCommentBuilder.toString();
        }

        private static int rotationToExifOrientation(int degrees) {
            int exifOrientation;
            switch (degrees) {
                case 0:
                    exifOrientation = 1; // 0CW
                    break;
                case 90:
                    exifOrientation = 6; // 270CW
                    break;
                case 180:
                    exifOrientation = 3; // 180CW
                    break;
                case 270:
                    exifOrientation = 8; // 90CW
                    break;
                default:
                    exifOrientation = 1; // 0CW
                    break;
            }

            return exifOrientation;
        }
    }

    public static class RequiredTags {
        public TiffField make;
        public TiffField model;
        public TiffField iso;
        public TiffField exposure;
        public TiffField aperture;
        public TiffField flash;
        public TiffField compressedBitsPerPixel;
        // User Comment is also required, but added manually
        // Orientation is also required, but added manually
    }
}
