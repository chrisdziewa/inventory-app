package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

public class InventoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PRODUCT_LOADER = 2;

    private ProductCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        final FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.add_product_button);

        // Opens the add a product version of Editor Activity when button is clicked
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editorIntent = new Intent(InventoryActivity.this, EditorActivity.class);
                startActivity(editorIntent);
            }
        });

        ListView productList = (ListView) findViewById(R.id.product_list);

        // Setup on item click listener to view product details
        productList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(InventoryActivity.this, ProductDetailActivity.class);
                Uri uri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);

                intent.setData(uri);
                startActivity(intent);
            }
        });


        mAdapter = new ProductCursorAdapter(this, null);
        productList.setAdapter(mAdapter);

        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case PRODUCT_LOADER:
                String[] projection = new String[] {
                        ProductEntry._ID,
                        ProductEntry.COLUUMN_PRODUCT_NAME,
                        ProductEntry.COLUMN_PRODUCT_PRICE,
                        ProductEntry.COLUMN_PRODUCT_QUANTITY,
                        ProductEntry.COLUMN_PROCUCT_SALES_TOTAL,
                        ProductEntry.COLUMN_PRODUCT_IMAGE
                };

                // get Cursor loader
                return new CursorLoader(getApplicationContext(), ProductEntry.CONTENT_URI, projection, null, null, null);
            default:
                // Loader not recognized
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mAdapter.swapCursor(null);
    }

}
