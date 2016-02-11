package com.sourceit.task2.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.sourceit.task2.R;
import com.sourceit.task2.model.Product;
import com.sourceit.task2.utils.L;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Button buutonPause;
    boolean connect = true;

    private MyAdapter myAdapter;
    private Random random = new Random();

    private boolean paused;

    private final int NUMBER_OF_GOODS = 50;
    private final int SLEEP_TIME = 500;

    private final int COUNT_BUYS = 2;
    private final int MAXIMUM_PURCHASE = 10;
    private final int NUMBER_OF_TYPES = 5;

    private int tempProductNumber;

    private LinkedList<Integer> positions = new LinkedList<>();
    private LinkedList<Product> products = new LinkedList<>();

    private ArrayList<Integer> requestsKeys = new ArrayList<>();
    private ArrayList<Integer> requestsValues = new ArrayList<>();

    private int[] numbersBuyProducts = new int[COUNT_BUYS];
    private int[] numberOfPurchasedGoods = new int[COUNT_BUYS];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buutonPause = (Button) findViewById(R.id.button_pause);

        for (int i = 0; i < 5; i++) {
            products.add(new Product("product" + i, NUMBER_OF_GOODS));
            positions.add(i);
        }

        ListView products_list = (ListView) findViewById(R.id.products_list);
        myAdapter = new MyAdapter(this, products);
        products_list.setAdapter(myAdapter);

        repeatBuys();

        buutonPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!paused) {
                    paused = true;
                } else {
                    paused = false;
                    connect = true;
                }
            }
        });
    }


    private class MyAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            L.d("porducts.size = " + products.size());
            if (products.size() < COUNT_BUYS) {
                countNumberOfPurchase(COUNT_BUYS - 1);
            } else countNumberOfPurchase(COUNT_BUYS);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (products.size() < COUNT_BUYS) {
                purchase(COUNT_BUYS - 1);
            } else purchase(COUNT_BUYS);

            repeatBuys();
        }
    }

    private void repeatBuys() {
        L.d("repeatBuys");
        myAdapter.notifyDataSetChanged();
        if (!products.isEmpty()) {
            new MyAsyncTask().execute();
        }
    }

    private void countNumberOfPurchase(int countBuys) {
        L.d("countBuys = " + countBuys);
        for (int i = 0; i < countBuys; i++) {
            if (i == COUNT_BUYS - 1) {
                tempProductNumber = countNymberOfAnotherProduct(numbersBuyProducts[0]);
            } else {
                tempProductNumber = randomTypes();
            }
            numbersBuyProducts[i] = tempProductNumber;
            L.d("numbersBuyProducts[i]" + numbersBuyProducts[i]);
            numberOfPurchasedGoods[i] = purchaseAmount(MAXIMUM_PURCHASE);
            L.d("numberOfPurchasedGoods[i]" + numberOfPurchasedGoods[i]);
        }
    }

    private int countNymberOfAnotherProduct(int anotherProduct) {
        int tempNumber = randomTypes();
        if (tempNumber == anotherProduct) {
            return countNymberOfAnotherProduct(anotherProduct);
        } else return tempNumber;
    }

    private int purchaseAmount(int maximumPurchase) {
        int tempCount = random.nextInt(maximumPurchase + 1);
        if (tempCount == 0) {
            return purchaseAmount(maximumPurchase + 1);
        } else return tempCount;
    }

    private byte doRequests() {
        for (int i = 0; i < requestsValues.size(); i++) {
            if (!products.isEmpty()) {
                if (positions.contains(requestsKeys.get(i))) {
                    products.get(positions.indexOf(requestsKeys.get(i))).count -= requestsValues.get(i);
                    if (products.get(positions.indexOf(requestsKeys.get(i))).count <= 0) {
                        products.remove(positions.indexOf(requestsKeys.get(i)));
                        positions.remove(positions.indexOf(requestsKeys.get(i)));
                    }
                }
            } else break;
        }
        return 0;
    }

    private void purchase(int countBuys) {

        for (int i = 0; i < countBuys; i++) {
            L.d("numbersBuyProducts " + numbersBuyProducts[i]);
            int buy = numberOfPurchasedGoods[i];

            if (!paused) {
                if (!requestsValues.isEmpty()) {
                    byte back = doRequests();
                }
                if (positions.contains(numbersBuyProducts[i])) {
                    products.get(positions.indexOf(numbersBuyProducts[i])).count -= buy;
                }
            } else {
                requestsValues.add(buy);
                requestsKeys.add(numbersBuyProducts[i]);
            }

            if (connect) {
                if (positions.contains(numbersBuyProducts[i])) {
                    if (products.get(positions.indexOf(numbersBuyProducts[i])).count <= 0) {
                        products.remove(positions.indexOf(numbersBuyProducts[i]));
                        positions.remove(positions.indexOf(numbersBuyProducts[i]));
                        L.d("products.size: " + products.size());
                        L.d("positions " + positions.toString());
                        if (paused) {
                            connect = false;
                        }
                    }
                }
            }
        }
        L.d("purchase out");
    }

    private int randomTypes() {
        int tempNumber = random.nextInt(NUMBER_OF_TYPES);
        L.d("tempNumber: " + tempNumber);
        if (positions.contains(tempNumber)) {
            L.d("positions:" + positions.toString());
            return tempNumber;
        } else return randomTypes();
    }
}
