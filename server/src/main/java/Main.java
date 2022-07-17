import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gson.reflect.TypeToken;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.*;
import com.google.gson.*;
import org.apache.xmlbeans.impl.xb.xsdschema.BlockSet;

import static spark.Spark.*;


public class Main {

    public static class LowStockItem {
        private int sku;
        private String itemName;
        private int amtInStock;
        private int capacity;

        public LowStockItem(int sku, String itemName, int amtInStock, int capacity) {
            this.sku = sku;
            this.itemName = itemName;
            this.amtInStock = amtInStock;
            this.capacity = capacity;
        }
    };

    public static class ItemOrder {
        private int sku;
        private int amtToOrder;

        public ItemOrder(int sku, int amtToOrder) {
            this.sku = sku;
            this.amtToOrder = amtToOrder;
        }
    }

    public static void main(String[] args) {

        //This is required to allow GET and POST requests with the header 'content-type'
        options("/*",
                (request, response) -> {
                    response.header("Access-Control-Allow-Headers",
                            "content-type");

                    response.header("Access-Control-Allow-Methods",
                            "GET, POST");


                    return "OK";
                });

        //This is required to allow the React app to communicate with this API
        before((request, response) -> response.header("Access-Control-Allow-Origin", "http://localhost:3000"));

        get("/hello", (req, res) -> "Hello World");



        //TODO: Return JSON containing the candies for which the stock is less than 25% of it's capacity
        get("/low-stock", (request, response) -> {
            ArrayList<ArrayList<Object>> table = readXLSX("Inventory.xlsx", 0);
//            printTable(table);

            ArrayList<LowStockItem> lowStockList = new ArrayList<LowStockItem>();

            for(int i = 1; i < table.size(); i++) {

                // percent scaled by 100
                double currInven = (double)(table.get(i).get(1)) / (double)(table.get(i).get(2)) * 100;

                if (currInven < 25) {

                    // (int)(double) is so the Object type in the form `xx.0` get typecasted to double then int
                    lowStockList.add(new LowStockItem((int)(double)table.get(i).get(3), (String)table.get(i).get(0),
                            (int)(double)table.get(i).get(1), (int)(double)table.get(i).get(2)));
                }
            }

//            // debugging: print lowstocklist
//            System.out.print(gson.toJson(lowStockList));

//            for(int i = 0; i < lowStockList.size(); i++) {
//                System.out.println(lowStockList.get(i));
//            }

            return new Gson().toJson(lowStockList);
        });

        //TODO: Return JSON containing the total cost of restocking candy
        post("/restock-cost", (request, response) -> {
            // creating hashmap of cheapest candy
            ArrayList<ArrayList<Object>> distr1 = readXLSX("Distributors.xlsx", 0);
            ArrayList<ArrayList<Object>> distr2 = readXLSX("Distributors.xlsx", 1);
            ArrayList<ArrayList<Object>> distr3 = readXLSX("Distributors.xlsx", 2);

            Map<Object, Object> leastCost = new HashMap<Object, Object>();

            leastCost = updateCosts(distr1, leastCost);
            leastCost = updateCosts(distr2, leastCost);
            leastCost = updateCosts(distr3, leastCost);

            // print least cost key
            for(Object key: leastCost.keySet()){
                System.out.println(key + " = " + leastCost.get(key));
            }

            // read current inventory and calculate cost
//            ArrayList<ArrayList<Object>> table = readXLSX("Inventory.xlsx", 0);
//            printTable(table);

            // parse request json
            Gson gson = new Gson();
            String orderJson = request.body();
            System.out.println(orderJson);

//            Type userListType = new TypeToken<ArrayList<ItemOrder>>(){}.getType();
//            ArrayList<ItemOrder> orderList = gson.fromJson(orderJson,userListType);

            ItemOrder[] orderList = gson.fromJson(orderJson, ItemOrder[].class);
            for(int i = 0; i < orderList.length; i++) {
                System.out.println(orderList[i].sku + ": " + orderList[i].amtToOrder);
            }

            double totalCost = 0.0;
//            double missingStock = 0;

            for(int i = 0; i < orderList.length; i++) {
//                missingStock = (double)table.get(i).get(2) - (double)table.get(i).get(1);
                System.out.println(leastCost.get(orderList[i].sku) + " * " + orderList[i].amtToOrder);
                totalCost += (double)leastCost.get(orderList[i].sku) * orderList[i].amtToOrder;
            }
//            System.out.println();
//            System.out.print(totalCost);




            return new Gson().toJson(totalCost);
        });

    }

    public static ArrayList<ArrayList<Object>> readXLSX (String str, int sheetNum) throws IOException {
        ArrayList<ArrayList<Object>> table = new ArrayList<ArrayList<Object>>();
        ArrayList<Object> tempRow = new ArrayList<Object>();

        File myFile = new File ("resources\\" + str);
        FileInputStream fis = new FileInputStream(myFile);

        XSSFWorkbook myWorkBook = new XSSFWorkbook(fis);
        XSSFSheet sheet = myWorkBook.getSheetAt(sheetNum);

        Iterator<Row> rowIterator = sheet.iterator();

        // boolean check for empty row
        boolean isEmpty = false;

        while(rowIterator.hasNext()) {
            Row itrRow = rowIterator.next();

            Iterator<Cell> cellIterator = itrRow.cellIterator();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();

                CellType type = cell.getCellType();

                switch(cell.getCellType()) {
                    case STRING:
                        String tempStr = cell.getStringCellValue();
//                        System.out.print(tempStr + "\t\t\t");
                        if(tempStr.trim() != "") {
                            tempRow.add(tempStr);
                        }
                        break;
                    case NUMERIC:
                        double tempInt = cell.getNumericCellValue();
//                        System.out.print(tempInt + "\t\t\t");
                        tempRow.add(tempInt);
                        break;
                    case BLANK:
                        isEmpty = true;
                        break;
                    case BOOLEAN:
                        System.out.print("hmmmm");
                        break;
                }
            }
//            System.out.println("");
            if(!isEmpty) {
                table.add(new ArrayList<>(tempRow));
            }
            tempRow.clear();
        }

        return table;
    }

    public static void printTable(ArrayList<ArrayList<Object>> table){
        for(int i = 0; i < table.size(); i++) {
            for(int j = 0; j <  table.get(i).size(); j++){
                System.out.print(table.get(i).get(j) + "\t");
            }
            System.out.println("");
        }
    }


    public static Map updateCosts(ArrayList<ArrayList<Object>> distr, Map<Object, Object> currCost) {

        for(int i = 1; i < distr.size(); i++) {
//            System.out.println(distr.get(i).get(0));
            int candysku = (int)(double) distr.get(i).get(1);
            Object newCost = distr.get(i).get(2);

            // if entry doesn't exist, add candy/cost pair
            if (!currCost.containsKey(candysku)) {
                currCost.put(candysku, newCost);
                continue;
            }

            // if current cost is greater than new cost, update entry
            if ((double) currCost.get(candysku) > (double) newCost) {
                currCost.put(candysku, newCost);
            }

        }

        return currCost;
    }

}