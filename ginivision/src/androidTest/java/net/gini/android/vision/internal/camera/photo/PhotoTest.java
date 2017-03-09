package net.gini.android.vision.internal.camera.photo;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.common.truth.Truth.assertThat;

import static net.gini.android.vision.test.Helpers.doParcelingRoundTrip;
import static net.gini.android.vision.test.Helpers.getTestJpeg;
import static net.gini.android.vision.test.PhotoSubject.photo;

import android.support.test.runner.AndroidJUnit4;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.UUID;

@RunWith(AndroidJUnit4.class)
public class PhotoTest {

    private static byte[] TEST_JPEG = null;

    @BeforeClass
    public static void setupClass() throws IOException {
        TEST_JPEG = getTestJpeg();
    }

    @AfterClass
    public static void teardownClass() throws IOException {
        TEST_JPEG = null;
    }

    @Test
    public void should_supportParceling() {
        // Given
        Photo photo = Photo.fromJpeg(TEST_JPEG, 0);
        // When
        Photo photoFromParcel = doParcelingRoundTrip(photo, Photo.CREATOR);
        // Then
        assertThat(photoFromParcel).isEqualTo(photo);
    }

    @Test
    public void should_generateUUID_whenCreated() {
        // When
        Photo photo = Photo.fromJpeg(TEST_JPEG, 0);
        // Then
        assertThat(UUID.fromString(photo.getUUID())).isNotNull();
    }

    @Test
    public void should_generate_uniqueUUIDs_forEachInstance() {
        // Given
        Photo photo1 = Photo.fromJpeg(TEST_JPEG, 0);
        Photo photo2 = Photo.fromJpeg(TEST_JPEG, 0);
        // Then
        assertThat(photo1.getUUID()).isNotEqualTo(photo2.getUUID());
    }

    @Test
    public void should_addUUID_toExifUserComment() {
        // Given
        Photo photo = Photo.fromJpeg(TEST_JPEG, 0);
        // Then
        assertAbout(photo()).that(photo).hasUUIDinUserComment(photo.getUUID());
    }

    @Test
    public void should_keepUUID_afterRotation() {
        // Given
        Photo photo = Photo.fromJpeg(TEST_JPEG, 0);
        String uuid = photo.getUUID();
        // When
        photo.edit().rotate(90).apply();
        // Then
        assertAbout(photo()).that(photo).hasUUIDinUserComment(uuid);
    }

    @Test
    public void should_keepUUID_afterCompression() {
        // Given
        Photo photo = Photo.fromJpeg(TEST_JPEG, 0);
        String uuid = photo.getUUID();
        // When
        photo.edit().compress(10).apply();
        // Then
        assertAbout(photo()).that(photo).hasUUIDinUserComment(uuid);
    }
}