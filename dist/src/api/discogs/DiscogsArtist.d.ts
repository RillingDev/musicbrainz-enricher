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
interface DiscogsArtist {
    namevariations: string[];
    profile: string;
    releases_url: string;
    resource_url: string;
    uri: string;
    urls: string[];
    data_quality: string;
    realname: string;
    id: number;
    images: DiscogsArtistImage[];
    members: DiscogsArtistMember[];
}
export { DiscogsArtist };
