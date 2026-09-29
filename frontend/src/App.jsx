import { useEffect, useState } from "react";
import {getTransactions, deleteTransaction, createTransaction} from "./services/financeApi";
import "./App.css";

function App() {
    const [transactions, setTransactions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

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

    async function handleDelete(id){
        try{
            await deleteTransaction(id);
            const data = await getTransactions();
            setTransactions(data);
        }catch(requestError){
            setError(requestError.message);
        }
    }

    async function handleCreate(transaction){
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
                            <th>Actions</th>

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
        </main>
    );
}

export default App;