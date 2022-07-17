import React from 'react'

export default function LowStockList ( props ) {

    return (
        <>
            {props.lowstocks.map((item) => (
                
                <tr key={item.itemName} >
                    
                        <td>{item.sku}{' '}</td>
                        <td>{item.itemName}{' '}</td>
                        <td>{item.amtInStock}{' '}</td>
                        <td>{item.capacity}</td>
                        <td>
                            <input 
                                id={item.sku}
                                onChange={props.handleChange}
                                ></input>
                        </td>
                    
                </tr>
            ))}
            {console.log(props.lowstocks)}
        </>
    )
}