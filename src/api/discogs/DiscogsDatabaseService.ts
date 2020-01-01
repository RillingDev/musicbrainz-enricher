import { Injectable } from "chevronjs";
import { Client as DiscogsClient } from "disconnect";
import { chevron } from "../../chevron";
import { AsyncService } from "../../util/AsyncService";
import { DiscogsArtist } from "./schema/DiscogsArtist";

@Injectable(chevron, { dependencies: [AsyncService] })
class DiscogsDatabaseService {
    private database: any;

    constructor(private readonly asyncService: AsyncService) {
        this.database = new DiscogsClient().database();
    }

    public async getArtist(id: string): Promise<DiscogsArtist> {
        await this.asyncService.throttle(1000);
        return this.database.getArtist(String(id));
    }
}

export { DiscogsDatabaseService };
