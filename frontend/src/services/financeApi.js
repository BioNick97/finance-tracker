export async function getTransactions() {
    const response = await fetch("/api/transactions");

    if (!response.ok) {
        throw new Error(`Request failed: ${response.status}`);
    }

    return response.json();
}
