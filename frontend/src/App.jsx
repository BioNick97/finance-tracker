import { useEffect, useState } from "react";
import {getTransactions, deleteTransaction, createTransaction, getCategories} from "./services/financeApi";

import "./App.css";

function App() {
    const [transactions, setTransactions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [isCreateOpen, setIsCreateOpen] = useState(false);
    const [description, setDescription] = useState("");
    const [amount, setAmount] = useState("");
    const [transactionDate, setTransactionDate] = useState("");
    const [categories, setCategories] = useState([]);
    const [categoryId, setCategoryId] = useState("");


    useEffect(() => {

        async function loadTransactions() {
            try {
                const data = await getTransactions();
                setTransactions(data);
            } catch (requestError) {
                setError(requestError.message);
            } finally {
                setLoading(false);
            }
        }

        loadTransactions();
        
    }, []);
    useEffect(() => {
        async function loadCategories() {
            try {
                const data = await getCategories();
                setCategories(data);
            } catch (requestError) {
                setError(requestError.message);
            }
        }

        loadCategories();
    }, []);

    async function handleDelete(id){
        try{
            await deleteTransaction(id);
            const data = await getTransactions();
            setTransactions(data);
        }catch(requestError){
            setError(requestError.message);
        }
    }

    async function doCreate(transaction){
        try{
            await createTransaction(transaction);
            const data = await getTransactions();
            setTransactions(data);
        }catch(requestError){
            setError(requestError.message);
        }
    }

    return (
        <main>
            <h1>Finance Tracker</h1>
            <p>Manage your income and expenses.</p>

            <h2>Transactions</h2>

            {loading && <p>Loading transactions...</p>}

            {error && <p>Unable to load transactions: {error}</p>}

            {!loading && !error && (
                <table>
                    <thead>
                        <tr>
                            <th>Date</th>
                            <th>Description</th>
                            <th>Category</th>
                            <th>Amount</th>
                            <th>Actions<button onClick={() => setIsCreateOpen(true)}>New</button>
                            </th>

                        </tr>

                    </thead>
                    <tbody>
                        {transactions.map((transaction) => (
                            <tr key={transaction.id}>
                                <td>{transaction.transactionDate}</td>
                                <td>{transaction.description}</td>
                                <td>{transaction.category.name}</td>
                                <td>{transaction.amount}</td>
                                <td><button onClick={() => handleDelete(transaction.id)}>Delete</button> </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
            {isCreateOpen && (
                <div>
                    <form onSubmit={(event)=>event.preventDefault()}>

                        <h2>New Transaction</h2>
                        <div>
                            <label htmlFor="description">Description:</label>
                            <input id="description" name="description" type="text"
                                   value={description}
                                   onChange={(event) => setDescription(event.target.value)}
                            />
                        </div>
                        <div>
                            <label htmlFor="amount">Amount:</label>
                            <input id="amount" name="amount" type="number" step="0.01"
                                   value={amount}
                                   onChange={(event) => setAmount(event.target.value)}
                            />
                        </div>
                        <div>
                            <label htmlFor="transactionDate">Date:</label>
                            <input id="transactionDate" name="transactionDate" type="date"
                            value ={transactionDate}
                            onChange={(event) => setTransactionDate(event.target.value)}/>
                        </div>
                        <button type="submit">Add</button>
                        <button type="button" onClick={() => setIsCreateOpen(false)}>Cancel</button>

                    </form>

                </div>
            )}

        </main>
    );
}

export default App;