# Topic
URL Shortener

**took 41 min**

# Functional & non-functional questions
1. How many links we should store? Affects the length of the result link
2. TTL?
3. What symbols we can use and what cannot
4. Should the result be stable across multiple users or single user? (bit.ly said no)

# Task
1. From LeetCode: Build a solution that receives a long URL and return a short URL. The params was the long url and a **passcode** that is used to build the short URL.
2. ChatGPT assumption: Build a very simple URL shortening service. You should support two operations: shorten(originalUrl): String and getOriginal(shortCode): String. Store mappings in-memory

# Source
- [Almost no details](https://leetcode.com/discuss/post/2187343/revolut-senior-software-engineer-india-j-2g3f/)
  - Few optimization and requirement changes to have alphanumeric values as path.