use icu::collator::Collator;
use icu::collator::options::{CollatorOptions, Strength};
use icu::locale::Locale;
use std::{cmp::Ordering, collections::HashSet};

/// Allows matching a string to its canonical form.
///
/// This is done by using a configurable `Collator` as well as a collection of substrings
/// representing common string variant delimiters in the english language, such as "-" (e.g., "hip-hop" vs "hip hop"),
/// and checking if two words are the same ignoring these delimiters.
///
/// Note that due to the complexity of language, this tool only covers basic cases.
#[derive(Debug)]
pub struct CanonicalStringMatcher {
	canonical_values: HashSet<String>,
	collator: Collator,
	ignored_sub_strings: Vec<String>,
}

impl CanonicalStringMatcher {
	pub(crate) fn new(
		canonical_values: HashSet<&str>,
		collator: Collator,
		ignored_sub_strings: HashSet<&str>,
	) -> Self {
		let mut ignored_sub_strings: Vec<String> = ignored_sub_strings
			.into_iter()
			.map(ToString::to_string)
			.collect();
		// Move longer strings to the beginning so they are replaced before short ones. This prevents issues
		// where a long substring contains some of the shorter substrings (e.g., " and " as well as " ").
		ignored_sub_strings.sort_by_key(|r| std::cmp::Reverse(r.len()));

		let canonical_values = canonical_values
			.into_iter()
			.map(ToString::to_string)
			.collect();

		Self {
			canonical_values,
			collator,
			ignored_sub_strings,
		}
	}

	// Attempt to get the canonical form of the provided value.
	pub fn canonicalize(&self, unmatched_value: &str) -> Option<String> {
		let collator = self.collator.as_borrowed();
		// First, search for exact matches
		self.canonical_values
			.iter()
			// We first search for exact matches.
			// This helps avoid situations where multiple canonical values have colliding replaced values.
			// In these cases a random canonical match would be picked, which is bad.
			.find(|canonical_value| {
				collator.compare(canonical_value, unmatched_value) == Ordering::Equal
			})
			// Only if no exact match is found, check with ignored characters replaced.
			.or_else(|| {
				self.canonical_values.iter().find(|canonical_value| {
					collator.compare(
						&Self::prepare_value(canonical_value, &self.ignored_sub_strings),
						&Self::prepare_value(unmatched_value, &self.ignored_sub_strings),
					) == Ordering::Equal
				})
			})
			.cloned()
	}

	fn prepare_value(value: &str, ignored_sub_strings: &[String]) -> String {
		ignored_sub_strings
			.iter()
			.fold(value.to_string(), |acc, substr| acc.replace(substr, ""))
	}
}

pub fn default_genre_canonical_string_matcher(
	canonical_genres: HashSet<&str>,
) -> anyhow::Result<CanonicalStringMatcher> {
	// Best-effort configuration to cover most common cases.
	let ignored_sub_strings = HashSet::from(["-", " ", " and ", " & "]);

	let mut collator_options = CollatorOptions::default();
	// While PRIMARY may seem fitting here, there are genres that would mistakenly be
	// treated as identical (https://musicbrainz.org/genre/57a6dcc1-c3cd-4ce1-9fb2-e1783992a683 and https://musicbrainz.org/genre/c1f813d2-d21f-4eda-85e3-e8bfac92b3e1).
	collator_options.strength = Some(Strength::Secondary);
	let collator = Collator::try_new(Locale::UNKNOWN.into(), collator_options)?;

	Ok(CanonicalStringMatcher::new(
		canonical_genres,
		collator.static_to_owned(),
		ignored_sub_strings,
	))
}

pub fn canonicalize_genres(
	unmatched_genres: &HashSet<&str>,
	canonical_string_matcher: &CanonicalStringMatcher,
) -> impl Iterator<Item = String> {
	unmatched_genres
		.iter()
		.filter_map(|unmatched_genre| canonical_string_matcher.canonicalize(unmatched_genre))
}

#[cfg(test)]
mod tests {
	use icu::collator::Collator;
	use icu::collator::options::*;
	use icu::locale::Locale;

	use super::*;

	#[test]
	fn canonicalize_returns_same_if_already_canonical() -> anyhow::Result<()> {
		let collator = Collator::try_new(Locale::UNKNOWN.into(), CollatorOptions::default())?;
		let matcher = CanonicalStringMatcher::new(
			HashSet::from(["hip-hop", "rock"]),
			collator.static_to_owned(),
			HashSet::from(["-", " "]),
		);

		assert_eq!(matcher.canonicalize("rock"), Some(String::from("rock")));
		assert_eq!(
			matcher.canonicalize("hip-hop"),
			Some(String::from("hip-hop"))
		);

		Ok(())
	}

	#[test]
	fn canonicalize_returns_canonicalized() -> anyhow::Result<()> {
		let matcher = CanonicalStringMatcher::new(
			HashSet::from(["rock", "hip-hop", "drum & bass"]),
			Collator::try_new(Locale::UNKNOWN.into(), CollatorOptions::default())?
				.static_to_owned(),
			HashSet::from(["-", " ", " & ", " and "]),
		);

		assert_eq!(
			matcher.canonicalize("hip hop"),
			Some(String::from("hip-hop"))
		);
		assert_eq!(
			matcher.canonicalize("drum and bass"),
			Some(String::from("drum & bass"))
		);

		Ok(())
	}

	#[test]
	fn canonicalize_returns_canonicalized_with_mixed_ignored_sub_strings() -> anyhow::Result<()> {
		let matcher = CanonicalStringMatcher::new(
			HashSet::from(["super-duper fancy and cool"]),
			Collator::try_new(Locale::UNKNOWN.into(), CollatorOptions::default())?
				.static_to_owned(),
			HashSet::from(["-", " ", " & ", " and "]),
		);

		assert_eq!(
			matcher.canonicalize("super duper fancy & cool"),
			Some(String::from("super-duper fancy and cool"))
		);

		Ok(())
	}

	#[test]
	fn canonicalize_respects_collator() -> anyhow::Result<()> {
		let ignored_sub_strings = HashSet::from(["-", " "]);

		let mut collator_es_options = CollatorOptions::default();
		collator_es_options.strength = Some(Strength::Primary);
		assert_eq!(
			CanonicalStringMatcher::new(
				HashSet::from(["yé-yé"]),
				Collator::try_new(Locale::UNKNOWN.into(), collator_es_options)?.static_to_owned(),
				ignored_sub_strings.clone()
			)
			.canonicalize("ye ye"),
			Some(String::from("yé-yé"))
		);

		let mut collator_options_secondary = CollatorOptions::default();
		collator_options_secondary.strength = Some(Strength::Secondary);
		assert_eq!(
			CanonicalStringMatcher::new(
				HashSet::from(["hip hop"]),
				Collator::try_new(Locale::UNKNOWN.into(), collator_options_secondary)?
					.static_to_owned(),
				ignored_sub_strings.clone()
			)
			.canonicalize("Hip Hop"),
			Some(String::from("hip hop"))
		);

		let mut collator_options_tertiary = CollatorOptions::default();
		collator_options_tertiary.strength = Some(Strength::Tertiary);
		assert_eq!(
			CanonicalStringMatcher::new(
				HashSet::from(["hip hop"]),
				Collator::try_new(Locale::UNKNOWN.into(), collator_options_tertiary)?
					.static_to_owned(),
				ignored_sub_strings.clone()
			)
			.canonicalize("Hip Hop"),
			None
		);

		Ok(())
	}

	#[test]
	fn canonicalize_returns_exact_when_canonical_collide_after_replacement() -> anyhow::Result<()> {
		let collator = Collator::try_new(Locale::UNKNOWN.into(), CollatorOptions::default())?;
		let matcher = CanonicalStringMatcher::new(
			HashSet::from(["hypertechno", "hyper techno"]),
			collator.static_to_owned(),
			HashSet::from([" "]),
		);

		assert_eq!(
			matcher.canonicalize("hypertechno"),
			Some(String::from("hypertechno"))
		);
		assert_eq!(
			matcher.canonicalize("hyper techno"),
			Some(String::from("hyper techno"))
		);

		Ok(())
	}

	#[test]
	fn canonicalize_genres_canonicalizes() -> anyhow::Result<()> {
		let matcher = default_genre_canonical_string_matcher(HashSet::from(["hip-hop", "rock"]))?;

		assert_eq!(
			canonicalize_genres(&HashSet::from(["hip hop", "jazz"]), &matcher)
				.collect::<Vec<String>>(),
			[String::from("hip-hop")]
		);

		Ok(())
	}
}
