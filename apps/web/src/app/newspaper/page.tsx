import type { Metadata } from "next";
import { NewspaperArchive } from "@/components/public/NewspaperArchive";

export const metadata: Metadata = { title: "Newspaper archive", description: "Read the latest published newspaper editions." };
export default function NewspaperPage() { return <NewspaperArchive />; }
