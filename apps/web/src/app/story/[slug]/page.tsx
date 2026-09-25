import type { Metadata } from "next";
import { StoryView } from "@/components/public/StoryView";

export async function generateMetadata({ params }: { params: Promise<{ slug: string }> }): Promise<Metadata> { const { slug } = await params; return { title: slug.replaceAll("-", " "), description: "Read the latest reporting from News Platform.", alternates: { canonical: `/story/${slug}` }, openGraph: { title: slug.replaceAll("-", " "), description: "Read the latest reporting from News Platform." } }; }
export default async function StoryPage({ params }: { params: Promise<{ slug: string }> }) { const { slug } = await params; return <StoryView slug={slug} />; }
