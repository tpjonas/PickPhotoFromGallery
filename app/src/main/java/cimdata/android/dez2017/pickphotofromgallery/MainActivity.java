package cimdata.android.dez2017.pickphotofromgallery;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaDataSource;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {


    private static final int REQUEST_CODE_PICK_PHOTO_INTENT = 1;
    private static final int REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE = 10;
    private static final String KEY_PARCEABLE_PHOTO_URI = "key.parceable.photoUri";

    Button chooseButton;
    ImageView pictureFrame;
    private Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        chooseButton = findViewById(R.id.btn_acmain_choose);
        pictureFrame = findViewById(R.id.img_acmain_picture);

        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // anstatt die Funktion gleich aufzurufwn, fragen wir erst die Permission ab
                // und rufen die Funktion aus dem Permission Dialog auf.
                requestStoragePermission();
                //pickPhotoFromGallery();

            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_PARCEABLE_PHOTO_URI)) {
                photoUri = savedInstanceState.getParcelable(KEY_PARCEABLE_PHOTO_URI);
                displayUriAsBitmap(photoUri);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putParcelable(KEY_PARCEABLE_PHOTO_URI, photoUri);

        super.onSaveInstanceState(outState);

    }

    private void requestStoragePermission() {

        // Hier schauen wir, ob wir die Permission schon haben
        boolean hasPermission = ContextCompat.checkSelfPermission(
                this,                               // Der Kontext
                Manifest.permission.READ_EXTERNAL_STORAGE   // Die Permission als Konstante aus dem Manifest
        )
                == PackageManager.PERMISSION_GRANTED;

        // Wenn wir die Permission schon haben, rufen wir unsere Funktion auf
        if (hasPermission) {
            pickPhotoFromGallery();
        }
        // Wenn wir die Permission nicht haben, fragen wir sie ab
        else {
            ActivityCompat.requestPermissions(
                    this, // Die Activity in der wir uns befinden
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE // Wie beim Intent eine Zahl, die den Request identifiziert

            );
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Als erstes schauen wir, obAnfrage zurückkommt
        if (requestCode == REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE) {

            // Wenn unsere Result-Liste Werte enthält
            // und wenn das erste Element PackageManager.PERMISSION_GRANTED ist
            boolean permissionGranted = /*grantResults.length > 0 && */ grantResults[0] == PackageManager.PERMISSION_GRANTED;

            // Wenn die Permission erteilt wird, zeigen wir den Dialog,
            // ansonsten den Toast
            if (permissionGranted) {
                pickPhotoFromGallery();
            } else {
                Toast.makeText(this, "Die Erlaubnis wurde nicht erteilt.", Toast.LENGTH_SHORT).show();
            }
            return;

        }

        Toast.makeText(this, "Ein unbekannter Request ...(Sollte nie toasten.)", Toast.LENGTH_SHORT).show();

    }

    private void pickPhotoFromGallery() {

        // Hier erstellen wir einen impliziten Intent.
        // Das heisst, dass wir eine Anfrage an das Systenm senden, aber nicht spezifizieren, wer das
        // Intent annimmt, sondern nur sagen was die können soll, damit sie das Intent annehmen kann.
        // (Damit eine App als eine App erkannt wird, die solch einen Intent annehmen kann, muss sie
        //  einen enstsprechenden Intent-Filter  in der manifest.xml implementieren. "Broadcast Reciever")
        // https://developer.android.com/reference/android/content/Intent.html

        // ACTION_PICK ist dafür da, eine Datei auszuwählen.
        // Wenn wir auf den Button klicken, werden alle Apps benachrichtigt, die den Filter für die
        // Action ACTION_PICK angemeldet haben. Auf den Emulatoren ist das nur GoogleDrive.
        // Wenn mehr als eine App für eine Action angemeldet ist, poppt ein Auswahl-Fenster auf, bei
        // dem man wählen kann, welche App das Intent annehmen soll.
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK);

        // Damit wir nicht irgendein Typ einer Datei wählen, müssen wir unseren Intent noch eingrenzen
        // indem wir den Typ der Datei spezifizieren, den wir zurückbekommen wollen
        pickPhotoIntent.setDataAndType(
                null,       // Hier könnten wir einen Ordner angeben, aus dem wir die Daten holen wollen.
                "image/*"   // Der Mime-Type der Datei, die wir bekommen wollen. (Allein hierdurch werden die Galerien zum öffnen angezeigt.)
        );

        // Hier schicken wir den Intent raus und sagen, dass wir etwas zurückbekommen wollen.
        startActivityForResult(
                pickPhotoIntent,
                REQUEST_CODE_PICK_PHOTO_INTENT  // Eine Zahl, an der wir den Intentbeim Eintreffen eines Ergebnis wieder
                // identifizieren können.
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Wir überprüfen, ob das Ergebnis zu unserem Intent passt
        if (requestCode == REQUEST_CODE_PICK_PHOTO_INTENT) {
            Toast.makeText(this, "Das Ergebnis ist da!", Toast.LENGTH_SHORT).show();

            if (data == null) {
                Toast.makeText(this, "Es gibt KEINE Daten!", Toast.LENGTH_SHORT).show();
                return;
            }
            // Hier verarbeiten wir die Daten, wenn welche da sind ...

            photoUri = data.getData();
            /*
            // setImageURI() läuft syncron ab. Wenn die Auswahl zu lange dauert, d.h. wenn der
            // UI-Thread länger als 5 sek geblockt ist, stürzt die App ab.
            pictureFrame.setImageURI(photoUri);
            */
            displayUriAsBitmap(photoUri);

        }
    }

    private void displayUriAsBitmap(Uri uri) {
        try {
            Bitmap bmp = MediaStore.Images.Media.getBitmap(
                    this.getContentResolver(),  // Hier übergebn wir einen ContenResolver, der für das Laden verantwortlich ist
                    uri                    // die Image URI
            );
            pictureFrame.setImageBitmap(bmp);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Das Laden hat nicht geklappt!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
