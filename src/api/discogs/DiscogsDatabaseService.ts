import { Injectable } from "chevronjs";
import { Client as DiscogsClient } from "disconnect";
import { chevron } from "../../chevron";
import { DiscogsArtist } from "./schema/DiscogsArtist";

@Injectable(chevron)
class DiscogsDatabaseService {
    private database: any;

    constructor() {
        this.database = new DiscogsClient().database();
    }

    public getArtist(id: string): Promise<DiscogsArtist> {
        return this.database.getArtist(String(id));
    }
}

export { DiscogsDatabaseService };
