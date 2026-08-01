import { useEffect, useState } from "react";
import { getTransactions } from "./services/financeApi";
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

    return (
        <main>
            <h1>Finance Tracker</h1>
            <p>Manage your income and expenses.</p>

            <h2>Transactions</h2>

            {loading && <p>Loading transactions...</p>}

            {error && <p>Unable to load transactions: {error}</p>}

            {!loading && !error && (
                <pre>{JSON.stringify(transactions, null, 2)}</pre>
            )}
        </main>
    );
}

export default App;