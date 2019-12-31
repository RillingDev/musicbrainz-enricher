import { User } from "discord.js";
declare class DiscordService {
    getFullUserName(user: User): string;
    isUserAllowed(allowedUserIds: string[] | null, author: User): boolean;
}
export { DiscordService };
