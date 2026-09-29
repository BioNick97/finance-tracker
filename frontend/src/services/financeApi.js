// API client for the Spring Boot backend.
// Every function here does the same three things:
//   1. call fetch() to send an HTTP request
//   2. check response.ok - if the server returned an error status (4xx/5xx),
//      response.ok is false, so we throw an Error instead of returning bad data
//   3. return response.json() to parse the response body back into a JS object
//
// GET requests only need a URL. POST/PUT need a method, a Content-Type header,
// and a JSON.stringify'd body. DELETE needs a method but no body, and this
// backend's DELETE returns 204 No Content, so there's nothing to parse back.

export async function getTransactions() {
    const response = await fetch('/api/transactions');

    if (!response.ok) {
        throw new Error(`Request failed: ${response.status}`);
    }

    return response.json();
}

export async function getSummary(month) {
    // Template literal + encodeURIComponent because "month" is a variable
    // being inserted into the URL's query string (e.g. ?month=2026-07).
    const response = await fetch(
        `/api/summary?month=${encodeURIComponent(month)}`
    );

    if (!response.ok) {
        throw new Error(`Request failed: ${response.status}`);
    }

    return response.json();
}

export async function getCategories(){
    const response = await fetch('/api/categories');

    if(!response.ok){
        throw new Error(`Request failed: ${response.status}`);
    }

    return response.json();

}

export async function createTransaction(transaction){
    // POST creates a new transaction. TransactionController expects the
    // new transaction's data as a JSON body (@RequestBody), so:
    //   - method must be POST
    //   - headers tell the server the body is JSON, not plain text
    //   - body must be a string, so JSON.stringify() converts the JS
    //     object into JSON text before it goes over the wire
    const response = await fetch('/api/transactions', {
        method: 'POST',
        headers:{
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(transaction)
    });

    if(!response.ok){
        throw new Error(`Request failed: ${response.status}`);
    }

    return response.json();

}

export async function deleteTransaction(id){
    // DELETE only needs to say *which* transaction to remove, so the id
    // goes in the URL path (template literal), not in a body.
    // TransactionController's delete endpoint returns 204 No Content on
    // success, meaning there is no response body - so unlike the other
    // functions, this one does NOT call response.json() at the end.
    // Calling .json() on an empty body would throw, even on success.
    const response = await fetch(`/api/transactions/${id}`,{
        method:'DELETE'
    })
    if(!response.ok){
        throw new Error(`Request failed: ${response.status}`);
    }

}

export async function updateTransaction(id, transaction){
    // PUT combines both ideas above: the id says WHERE (URL path, like
    // deleteTransaction), and the transaction object says WHAT the new
    // values should be (JSON body, like createTransaction).
    const response = await fetch(`/api/transactions/${id}`,{
        method:'PUT',
        headers:{
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(transaction)

    })
    if(!response.ok){
        throw new Error(`Request failed: ${response.status}`);
    }

    return response.json();
}
