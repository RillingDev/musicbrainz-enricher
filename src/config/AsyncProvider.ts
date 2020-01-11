interface AsyncProvider<T> {
    getInstance(): Promise<T>;
}

export { AsyncProvider };
