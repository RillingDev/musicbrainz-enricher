declare class JsonStorageService {
    hasStorageKey(path: string, key: string): Promise<boolean>;
    load(path: string, key: string): Promise<null | any>;
    store(path: string, key: string, data: any): Promise<void>;
    private hasStorage;
    private loadAll;
    private initStorage;
}
export { JsonStorageService };
