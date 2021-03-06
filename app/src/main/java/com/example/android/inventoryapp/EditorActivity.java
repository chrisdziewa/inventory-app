package com.example.android.inventoryapp;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private Uri mUri;

    private static final int SINGLE_PRODUCT_LOADER = 3;

    // reference EditText widgets
    private EditText mEditName;

    private EditText mEditPrice;

    private EditText mEditQuantity;

    private byte[] mImageByteArray;

    private Button mImageButton;

    private ImageView mProductImageView;

    private static final int IMAGE_PICKER_CODE = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mUri = getIntent().getData();

        // Initialize edit texts
        mEditName = (EditText) findViewById(R.id.product_name_edit_text);
        mEditPrice = (EditText) findViewById(R.id.price_edit_text);
        mEditQuantity = (EditText) findViewById(R.id.quantity_edit_text);

        // Initialize image view
        mProductImageView = (ImageView) findViewById(R.id.product_image);

        // Hide image on initialize
        mProductImageView.setVisibility(View.GONE);

        // Change action bar title
        if (mUri != null) {
            setTitle("Product Info");

            getLoaderManager().initLoader(SINGLE_PRODUCT_LOADER, null, this);
        } else {
            setTitle(getString(R.string.add_activity));
        }

        // Setup image upload button
        mImageButton = (Button) findViewById(R.id.image_upload_button);
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create image picker intent
                // Several helpful answers on here: http://stackoverflow.com/questions/5309190/android-pick-images-from-gallery
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                Intent chooserIntent = Intent.createChooser(intent, "Select an Image");
                startActivityForResult(chooserIntent, IMAGE_PICKER_CODE);
            }
        });
    }

    /**
     * Inserts a new product into the database
     *
     * @param name           of the product
     * @param price          of the product
     * @param quantity       of the product in stock
     * @param imageByteArray for the compressed bitmap image
     */
    private void addProduct(String name, int price, int quantity, byte[] imageByteArray) {
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUUMN_PRODUCT_NAME, name);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, price);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);

        if (imageByteArray != null) {
            values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, imageByteArray);
        }

        Uri uri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

        if (uri == null) {
            Toast.makeText(getApplicationContext(), "Product could not be created", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Product has been created", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    /**
     * Updates an existing product in the database
     *
     * @param name           of the product
     * @param price          of the product
     * @param quantity       of the product in stock
     * @param imageByteArray for the compressed bitmap image
     */
    private void editProduct(String name, int price, int quantity, byte[] imageByteArray) {

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUUMN_PRODUCT_NAME, name);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, price);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);

        if (imageByteArray != null) {
            values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, imageByteArray);
        }

        int rowsAffected = getContentResolver().update(mUri, values, null, null);

        if (rowsAffected == 0) {
            Toast.makeText(getApplicationContext(), "No changes were saved", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Product has been updated", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        String[] projection = new String[]{
                ProductEntry._ID,
                ProductEntry.COLUUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_IMAGE
        };

        switch (loaderId) {
            case SINGLE_PRODUCT_LOADER:
                return new CursorLoader(
                        getApplicationContext(),
                        mUri,
                        projection,
                        null,
                        null,
                        null
                );

            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.moveToNext()) {
            // Extract data from cursor
            String name = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUUMN_PRODUCT_NAME));

            int priceIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            Double price = CursorHelper.intToDecimal(cursor, priceIndex);
            String priceString = CursorHelper.doubleToDecimalString(price);

            String quantity = String.valueOf(cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY)));

            // Get Image if available
            mImageByteArray = cursor.getBlob(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE));
            Bitmap productImage = ImageHelper.convertBlobToBitmap(mImageByteArray);

            // set Image
            if (productImage != null) {
                mProductImageView.setImageBitmap(productImage);
                mProductImageView.setVisibility(View.VISIBLE);
            } else {
                mProductImageView.setVisibility(View.GONE);
            }

            // set edit text values
            mEditName.setText(name);
            mEditPrice.setText(priceString);
            mEditQuantity.setText(quantity);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mEditName.setText("");
        mEditPrice.setText("");
        mEditQuantity.setText("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICKER_CODE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return;
            }
            // Otherwise get image out of data
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                mProductImageView.setImageBitmap(selectedImage);
                // Temp code to store image
                mImageByteArray = ImageHelper.convertBitmapToBlob(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(EditorActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == IMAGE_PICKER_CODE) {
            Toast.makeText(EditorActivity.this, "You haven't picked an image", Toast.LENGTH_LONG).show();
        }
    }

    // Create options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.editor_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Add save action to action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveProduct();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // Save product in database
    private void saveProduct() {

        // Get values
        String name = mEditName.getText().toString();
        // Save price multiplied by 100 to save without decimals for accuracy
        int price = (int) (parseDouble(mEditPrice.getText().toString()) * 100);
        int quantity = parseInt(mEditQuantity.getText().toString());

        if (!validateInput(name, price, quantity)) {
            return;
        }

        if (mUri == null) {
            addProduct(name, price, quantity, mImageByteArray);
        } else {
            editProduct(name, price, quantity, mImageByteArray);
        }
    }

    // Make sure data is valid
    private boolean validateInput(String name, int price, int quantity) {
        ArrayList<String> resultList = new ArrayList<String>();
        if (TextUtils.isEmpty(name)) {
            resultList.add("name");
        }

        if (Float.isNaN(price) || price < 0) {
            resultList.add("price");
        }

        if (Float.isNaN(quantity) || quantity < 0) {
            resultList.add("quantity");
        }

        String negativeToast = TextUtils.join(", ", resultList);

        if (resultList.size() > 0) {
            Toast.makeText(getApplicationContext(), "Invalid " + negativeToast, Toast.LENGTH_SHORT).show();
            return false;
        }

        // Otherwise the validations passed
        return true;
    }
}