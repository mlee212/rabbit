import React, { useState } from 'react';
import ItemRow from './ItemRow'

export default function Challenge() {
  const [lowstock, setLowStock] = useState([]);
  const [totalCost, setTotalCost] = useState(0.00);
  // const [reorderList, setreorderList] = useState([]);

  var restockItems = {}
  lowstock.forEach(item => {
      restockItems[item.sku] = "0";
  });

  function handleChange(e) {
      restockItems[e.target.id] = e.target.value;
      console.log(e.target.id)
      console.log(JSON.stringify(restockItems))
  }

  function getLowStock() {
    fetch('http://localhost:4567/low-stock')
    .then(res => res.json())
    .then((data) => {
      setLowStock(data)
      // console.log(lowstock)
      console.log(restockItems)
    })
    .catch(console.log)
  }

  function orderStock() {
    // console.log(JSON.stringify(restockItems))
    var orderList = []
    for(const[key, value] of Object.entries(restockItems)) {
      orderList.push({sku : key, amtToOrder : value})
    }
    console.log(JSON.stringify(orderList))
    fetch('http://localhost:4567/restock-cost', {
      method: 'POST',
      body: JSON.stringify(orderList)
    }).then(res => res.json())
    .then((data) => {
      console.log(data)
      setTotalCost(data.toFixed(2))
    })
    .catch(console.log)
  }

  return (
    <>
      <table>
        <thead>
          <tr>
            <td>SKU</td>
            <td>Item Name</td>
            <td>Amount in Stock</td>
            <td>Capacity</td>
            <td>Order Amount</td>
          </tr>
        </thead>
        <tbody>
          
          {}
          {/* 
          TODO: Create an <ItemRow /> component that's rendered for every inventory item. The component
          will need an input element in the Order Amount column that will take in the order amount and 
          update the application state appropriately.
          */}
          <ItemRow lowstocks={lowstock} handleChange={handleChange}/>
        </tbody>
      </table>
      {/* TODO: Display total cost returned from the server */}
      <div>Total Cost: ${totalCost}</div>
      {/* 
      TODO: Add event handlers to these buttons that use the Java API to perform their relative actions.
      */}
      <button onClick={getLowStock}>Get Low-Stock Items</button>
      <button onClick={orderStock}>Determine Re-Order Cost</button>
      
    </>
  );
}
