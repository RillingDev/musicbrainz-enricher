interface DiscogsArtistImage {
    height: number;
    resource_url: string;
    type: string;
    uri: string;
    uri150: string;
    width: number;
}
interface DiscogsArtistMember {
    active: boolean;
    id: number;
    name: string;
    resource_url: string;
}
interface DiscogsArtistAlias {
    resource_url: string;
    id: number;
    name: string;
}
interface DiscogsArtist {
    namevariations: string[];
    profile: string;
    releases_url: string;
    resource_url: string;
    uri: string;
    urls?: string[];
    aliases?: DiscogsArtistAlias[];
    data_quality: string;
    realname?: string;
    id: number;
    images: DiscogsArtistImage[];
    members?: DiscogsArtistMember[];
}
export { DiscogsArtist };
